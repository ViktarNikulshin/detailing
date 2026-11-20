package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.FinanceType;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.PCCategory;
import com.nikulshin.detailing.model.domain.PCDirection;
import com.nikulshin.detailing.model.domain.PCSourceType;
import com.nikulshin.detailing.model.domain.PaymentCalendarBalance;
import com.nikulshin.detailing.model.domain.PaymentCalendarOperation;
import com.nikulshin.detailing.model.domain.PaymentCalendarSettings;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcBalanceDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcBalanceRequestDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcDayDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcOperationDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcOperationSaveDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcPeriodDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcSettingsDto;
import com.nikulshin.detailing.repository.OrderRepository;
import com.nikulshin.detailing.repository.PaymentCalendarBalanceRepository;
import com.nikulshin.detailing.repository.PaymentCalendarOperationRepository;
import com.nikulshin.detailing.repository.PaymentCalendarSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис «Платёжный календарь»: движение денег, остатки и кассовые разрывы.
 * Поступления по заказам подтягиваются автоматически из завершённых заказов
 * (без дублей), остальные операции — ручные. Все данные персистируются в БД.
 */
@Service
@RequiredArgsConstructor
public class PaymentCalendarService {

    /**
     * Счета в фиксированном порядке отображения.
     */
    private static final List<FinanceType> ACCOUNTS =
            List.of(FinanceType.CASH, FinanceType.NON_CASH, FinanceType.CURRENT_ACCOUNT);

    private static final Set<PCCategory> INCOME_CATEGORIES = Set.of(PCCategory.CLIENT_ORDER, PCCategory.OTHER_INCOME);

    private final PaymentCalendarOperationRepository operationRepository;
    private final PaymentCalendarBalanceRepository balanceRepository;
    private final PaymentCalendarSettingsRepository settingsRepository;
    private final OrderRepository orderRepository;

    // ------------------------------------------------------------------
    // Периоды и основной расчёт
    // ------------------------------------------------------------------

    @Transactional
    public PcPeriodDto getMonth(Integer year, Integer month) {
        YearMonth ym = YearMonth.of(year, month);
        return buildPeriod(ym.atDay(1), ym.atEndOfMonth(), "MONTH");
    }

    @Transactional
    public PcPeriodDto getWeek(LocalDate anyDate) {
        // Неделя с понедельника (согласовано с фронтендом: isoWeek)
        LocalDate start = anyDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        return buildPeriod(start, end, "WEEK");
    }

    private PcPeriodDto buildPeriod(LocalDate start, LocalDate end, String mode) {
        // 1. Идемпотентная синхронизация поступлений из завершённых заказов
        syncCompletedOrders(start, end);

        // 2. Операции периода
        List<PaymentCalendarOperation> operations =
                operationRepository.findByOperationDateBetweenOrderByOperationDateAsc(start, end);

        // 3. Входящие остатки по каждому счёту (с переносом из прошлых периодов)
        Map<FinanceType, BigDecimal> openingByAccount = new EnumMap<>(FinanceType.class);
        for (FinanceType account : ACCOUNTS) {
            openingByAccount.put(account, resolveOpeningBalance(start, account));
        }
        BigDecimal totalOpening = sum(openingByAccount.values());

        // 4. Дневная сетка: поступления, платежи, дневной баланс, кумулятив, кассовый разрыв
        List<PcDayDto> days = PaymentCalendarCalculator.buildDays(
                start, end, PaymentCalendarCalculator.groupByDate(operations), totalOpening);

        // 5. Итоги периода
        PaymentCalendarCalculator.Totals totals = PaymentCalendarCalculator.totals(operations);
        boolean hasCashGap = days.stream().anyMatch(PcDayDto::cashGap);

        // 6. Остатки по счетам: входящий, чистый поток, исходящий
        List<PcBalanceDto> balances = new ArrayList<>();
        for (FinanceType account : ACCOUNTS) {
            BigDecimal opening = openingByAccount.get(account);
            BigDecimal net = nz(operationRepository.netBetween(account, start, end.plusDays(1)));
            balances.add(new PcBalanceDto(account.name(), scale(opening), scale(net), scale(opening.add(net))));
        }
        BigDecimal totalNet = balances.stream().map(PcBalanceDto::net).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PcPeriodDto(mode, start, end, balances, days,
                totals.incomes(), totals.payments(),
                scale(totalOpening), scale(totalOpening.add(totalNet)), hasCashGap);
    }

    // ------------------------------------------------------------------
    // Стартовые остатки (с переносом из прошлых периодов)
    // ------------------------------------------------------------------

    /**
     * Остаток счёта на дату: ближайший явный стартовый остаток в прошлом
     * (или в текущем месяце) плюс чистый поток операций от 1-го числа его
     * месяца до {@code date}. Явный остаток трактуется как баланс на начало
     * своего месяца. Если явных остатков нет вовсе — чистый поток всех
     * операций с 2000-01-01 (перенос от нуля).
     */
    @Transactional(readOnly = true)
    public BigDecimal resolveOpeningBalance(LocalDate date, FinanceType account) {
        YearMonth current = YearMonth.from(date);
        for (int i = 0; i < 120; i++) {
            YearMonth candidate = current.minusMonths(i);
            Optional<PaymentCalendarBalance> explicit = balanceRepository
                    .findByYearAndMonthAndAccountId(candidate.getYear(), candidate.getMonthValue(), account);
            if (explicit.isPresent()) {
                BigDecimal net = nz(operationRepository.netBetween(account, candidate.atDay(1), date));
                return scale(explicit.get().getStartingBalance().add(net));
            }
            if (candidate.equals(YearMonth.of(2000, 1))) {
                break;
            }
        }
        return scale(nz(operationRepository.netBetween(account, LocalDate.of(2000, 1, 1), date)));
    }

    /**
     * Явно заданные стартовые остатки месяца (для редактора на UI).
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getExplicitStartingBalances(Integer year, Integer month) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (FinanceType account : ACCOUNTS) {
            result.put(account.name(), balanceRepository
                    .findByYearAndMonthAndAccountId(year, month, account)
                    .map(PaymentCalendarBalance::getStartingBalance)
                    .orElse(null));
        }
        return result;
    }

    @Transactional
    public void saveBalance(PcBalanceRequestDto request) {
        PaymentCalendarBalance balance = balanceRepository
                .findByYearAndMonthAndAccountId(request.year(), request.month(), request.account())
                .orElseGet(PaymentCalendarBalance::new);
        balance.setYear(request.year());
        balance.setMonth(request.month());
        balance.setAccountId(request.account());
        balance.setStartingBalance(scale(request.amount()));
        balanceRepository.save(balance);
    }

    // ------------------------------------------------------------------
    // Ручные операции (CRUD)
    // ------------------------------------------------------------------

    @Transactional
    public PcOperationDto createOperation(PcOperationSaveDto dto) {
        validateManual(dto);
        PaymentCalendarOperation op = new PaymentCalendarOperation();
        applyManual(op, dto);
        return toDto(operationRepository.save(op));
    }

    @Transactional
    public PcOperationDto updateOperation(Long id, PcOperationSaveDto dto) {
        PaymentCalendarOperation op = operationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Операция не найдена: " + id));
        if (op.getSourceType() == PCSourceType.ORDER_AUTO) {
            throw new IllegalArgumentException("Автоматические операции из заказов редактировать нельзя");
        }
        validateManual(dto);
        applyManual(op, dto);
        return toDto(operationRepository.save(op));
    }

    @Transactional
    public void deleteOperation(Long id) {
        PaymentCalendarOperation op = operationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Операция не найдена: " + id));
        if (op.getSourceType() == PCSourceType.ORDER_AUTO) {
            throw new IllegalArgumentException("Автоматические операции из заказов удалять нельзя");
        }
        operationRepository.delete(op);
    }

    @Transactional(readOnly = true)
    public List<PcOperationDto> getOperations(LocalDate start, LocalDate end) {
        return operationRepository.findByOperationDateBetweenOrderByOperationDateAsc(start, end)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private void validateManual(PcOperationSaveDto dto) {
        if (dto.category() == PCCategory.CLIENT_ORDER) {
            throw new IllegalArgumentException(
                    "Статья «Заказы» заполняется автоматически из завершённых заказов");
        }
        PCDirection expected = INCOME_CATEGORIES.contains(dto.category()) ? PCDirection.INCOME : PCDirection.EXPENSE;
        if (dto.direction() != expected) {
            throw new IllegalArgumentException(
                    "Для статьи " + dto.category() + " направление должно быть " + expected);
        }
    }

    private void applyManual(PaymentCalendarOperation op, PcOperationSaveDto dto) {
        op.setOperationDate(dto.date());
        op.setDirection(dto.direction());
        op.setCategory(dto.category());
        op.setAccountId(dto.account());
        op.setAmount(scale(dto.amount()));
        op.setDescription(dto.description());
        op.setSourceType(PCSourceType.MANUAL);
        op.setOrderId(null);
    }

    // ------------------------------------------------------------------
    // Синхронизация завершённых заказов (без дублей)
    // ------------------------------------------------------------------

    /**
     * Идемпотентно подтягивает поступления по завершённым заказам периода.
     * Для каждого заказа существует ровно одна авто-операция — уникальное
     * ограничение (source_type, order_id) + поиск существующей по order_id.
     * При изменении суммы/даты заказа операция обновляется; при отмене заказа
     * или переносе даты из периода осиротевшая операция удаляется.
     */
    @Transactional
    public void syncCompletedOrders(LocalDate start, LocalDate end) {
        FinanceType defaultAccount = getSettingsEntity().getDefaultIncomeAccount();

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay();

        List<Order> completed = orderRepository
                .findByDeliveryDateGreaterThanEqualAndDeliveryDateLessThan(from, to);

        Map<Long, Order> completedById = completed.stream()
                .collect(Collectors.toMap(Order::getId, Function.identity(), (a, b) -> a));

        // Существующие авто-операции этих заказов (могут жить в другом периоде — тогда переносим)
        Map<Long, PaymentCalendarOperation> existingByOrderId = completedById.isEmpty()
                ? Map.of()
                : operationRepository.findBySourceTypeAndOrderIdIn(PCSourceType.ORDER_AUTO, completedById.keySet())
                .stream()
                .collect(Collectors.toMap(PaymentCalendarOperation::getOrderId, Function.identity(), (a, b) -> a));

        for (Order order : completed) {
            if (order.getOrderCost() == null) {
                continue; // суммы нет — подтягивать нечего
            }
            PaymentCalendarOperation op = existingByOrderId.get(order.getId());
            if (op == null) {
                op = new PaymentCalendarOperation();
                op.setSourceType(PCSourceType.ORDER_AUTO);
                op.setOrderId(order.getId());
                op.setDirection(PCDirection.INCOME);
                op.setCategory(PCCategory.CLIENT_ORDER);
            }
            op.setOperationDate(paymentDate(order));
            op.setAccountId(defaultAccount);
            op.setAmount(BigDecimal.valueOf(order.getOrderCost()));
            op.setDescription(buildOrderDescription(order));
            operationRepository.save(op);
        }

        // Осиротевшие авто-операции в периоде: заказ отменён / незавершён / удалён
        List<Long> orphanIds = operationRepository
                .findBySourceTypeAndOperationDateBetween(PCSourceType.ORDER_AUTO, start, end)
                .stream()
                .filter(op -> op.getOrderId() != null && !completedById.containsKey(op.getOrderId()))
                .map(PaymentCalendarOperation::getId)
                .collect(Collectors.toList());
        if (!orphanIds.isEmpty()) {
            operationRepository.deleteAllById(orphanIds);
        }
    }

    /**
     * Дата поступления: дата завершения работ, иначе дата прибытия, иначе сегодня.
     */
    private LocalDate paymentDate(Order order) {
        LocalDateTime d = order.getDeliveryDate() != null ? order.getDeliveryDate() : order.getArrivalDate();
        return (d != null ? d : LocalDateTime.now()).toLocalDate();
    }

    private String buildOrderDescription(Order order) {
        String client = order.getClient() != null
                ? order.getClient().getName()
                : "Клиент #" + order.getId();
        String car = order.getCarBrand() != null ? " · " + order.getCarBrand() : "";
        return "Заказ #" + order.getId() + " · " + client + car;
    }

    // ------------------------------------------------------------------
    // Настройки
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PcSettingsDto getSettings() {
        return new PcSettingsDto(getSettingsEntity().getDefaultIncomeAccount());
    }

    @Transactional
    public PcSettingsDto updateSettings(PcSettingsDto dto) {
        PaymentCalendarSettings settings = getSettingsEntity();
        settings.setDefaultIncomeAccount(dto.defaultIncomeAccount());
        return new PcSettingsDto(settingsRepository.save(settings).getDefaultIncomeAccount());
    }

    private PaymentCalendarSettings getSettingsEntity() {
        return settingsRepository.findTopByOrderByIdAsc().orElseGet(() -> {
            PaymentCalendarSettings created = new PaymentCalendarSettings();
            created.setDefaultIncomeAccount(FinanceType.CASH);
            return settingsRepository.save(created);
        });
    }

    // ------------------------------------------------------------------
    // Утилиты
    // ------------------------------------------------------------------

    private PcOperationDto toDto(PaymentCalendarOperation op) {
        String client = null;
        String car = null;
        if (op.getOrderId() != null) {
            Order order = orderRepository.findById(op.getOrderId()).orElse(null);
            if (order != null) {
                client = order.getClient() != null ? order.getClient().getName() : null;
                car = order.getCarBrand();
            }
        }
        return new PcOperationDto(op.getId(), op.getOperationDate(), op.getDirection().name(),
                op.getCategory().name(), op.getAccountId().name(), scale(op.getAmount()),
                op.getDescription(), op.getSourceType().name(), op.getOrderId(), client, car);
    }

    private static BigDecimal sum(Collection<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static BigDecimal scale(BigDecimal value) {
        return nz(value).setScale(2, RoundingMode.HALF_UP);
    }
}
