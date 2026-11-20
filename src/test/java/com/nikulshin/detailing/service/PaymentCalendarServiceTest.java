package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.Client;
import com.nikulshin.detailing.model.domain.FinanceType;
import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.model.domain.PCCategory;
import com.nikulshin.detailing.model.domain.PCDirection;
import com.nikulshin.detailing.model.domain.PCSourceType;
import com.nikulshin.detailing.model.domain.PaymentCalendarOperation;
import com.nikulshin.detailing.model.domain.PaymentCalendarSettings;
import com.nikulshin.detailing.repository.OrderRepository;
import com.nikulshin.detailing.repository.PaymentCalendarBalanceRepository;
import com.nikulshin.detailing.repository.PaymentCalendarOperationRepository;
import com.nikulshin.detailing.repository.PaymentCalendarSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты синхронизации завершённых заказов (без дублей) и CRUD-правил операций.
 */
@ExtendWith(MockitoExtension.class)
class PaymentCalendarServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 1);
    private static final LocalDate END = LocalDate.of(2026, 11, 30);

    @Mock
    private PaymentCalendarOperationRepository operationRepository;
    @Mock
    private PaymentCalendarBalanceRepository balanceRepository;
    @Mock
    private PaymentCalendarSettingsRepository settingsRepository;
    @Mock
    private OrderRepository orderRepository;

    private PaymentCalendarService service;

    @BeforeEach
    void setUp() {
        service = new PaymentCalendarService(
                operationRepository, balanceRepository, settingsRepository, orderRepository);
        PaymentCalendarSettings settings = new PaymentCalendarSettings();
        settings.setDefaultIncomeAccount(FinanceType.CASH);
        // lenient: не все тесты доходят до чтения настроек
        lenient().when(settingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings));
    }

    private static Order completedOrder(long id, int cost, LocalDateTime delivery) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderStatus.COMPLETED);
        order.setOrderCost(cost);
        order.setDeliveryDate(delivery);
        order.setCarBrand("BMW X5");
        Client client = new Client();
        client.setName("Иван");
        order.setClient(client);
        return order;
    }

    @Test
    void syncCreatesSingleAutoOperationPerCompletedOrder() {
        Order order = completedOrder(1L, 500, LocalDateTime.of(2026, 11, 10, 15, 0));
        when(orderRepository.findByDeliveryDateGreaterThanEqualAndDeliveryDateLessThan(
                 any(), any())).thenReturn(List.of(order));
        when(operationRepository.findBySourceTypeAndOrderIdIn(any(), anyCollection())).thenReturn(List.of());
        when(operationRepository.findBySourceTypeAndOperationDateBetween(any(), any(), any())).thenReturn(List.of());
        when(operationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.syncCompletedOrders(START, END);

        ArgumentCaptor<PaymentCalendarOperation> captor = ArgumentCaptor.forClass(PaymentCalendarOperation.class);
        verify(operationRepository).save(captor.capture());

        PaymentCalendarOperation saved = captor.getValue();
        assertThat(saved.getSourceType()).isEqualTo(PCSourceType.ORDER_AUTO);
        assertThat(saved.getOrderId()).isEqualTo(1L);
        assertThat(saved.getDirection()).isEqualTo(PCDirection.INCOME);
        assertThat(saved.getCategory()).isEqualTo(PCCategory.CLIENT_ORDER);
        assertThat(saved.getAccountId()).isEqualTo(FinanceType.CASH); // счёт по умолчанию из настроек
        assertThat(saved.getAmount()).isEqualByComparingTo("500");
        assertThat(saved.getOperationDate()).isEqualTo(LocalDate.of(2026, 11, 10)); // по дате завершения
        assertThat(saved.getDescription()).contains("Заказ #1").contains("Иван").contains("BMW X5");
    }

    @Test
    void syncUpdatesExistingOperationInsteadOfCreatingDuplicate() {
        Order order = completedOrder(7L, 1200, LocalDateTime.of(2026, 11, 5, 12, 0));
        when(orderRepository.findByDeliveryDateGreaterThanEqualAndDeliveryDateLessThan(
                any(), any())).thenReturn(List.of(order));

        PaymentCalendarOperation existing = new PaymentCalendarOperation();
        existing.setId(42L);
        existing.setSourceType(PCSourceType.ORDER_AUTO);
        existing.setOrderId(7L);
        existing.setAmount(new BigDecimal("900")); // старая сумма — должна обновиться
        when(operationRepository.findBySourceTypeAndOrderIdIn(any(), anyCollection()))
                .thenReturn(List.of(existing));
        when(operationRepository.findBySourceTypeAndOperationDateBetween(any(), any(), any())).thenReturn(List.of());
        when(operationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.syncCompletedOrders(START, END);

        ArgumentCaptor<PaymentCalendarOperation> captor = ArgumentCaptor.forClass(PaymentCalendarOperation.class);
        verify(operationRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(42L); // та же запись, не новая
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("1200");
    }

    @Test
    void syncDeletesOrphanedOperationWhenOrderCancelled() {
        when(orderRepository.findByDeliveryDateGreaterThanEqualAndDeliveryDateLessThan(
                any(), any())).thenReturn(List.of());
        // completedById пуст — findBySourceTypeAndOrderIdIn не вызывается
        lenient().when(operationRepository.findBySourceTypeAndOrderIdIn(any(), anyCollection()))
                .thenReturn(List.of());

        PaymentCalendarOperation orphan = new PaymentCalendarOperation();
        orphan.setId(99L);
        orphan.setSourceType(PCSourceType.ORDER_AUTO);
        orphan.setOrderId(5L);
        lenient().when(operationRepository.findBySourceTypeAndOperationDateBetween(
                eq(PCSourceType.ORDER_AUTO), eq(START), eq(END))).thenReturn(List.of(orphan));

        service.syncCompletedOrders(START, END);

        verify(operationRepository).deleteAllById(List.of(99L));
    }

    @Test
    void syncSkipsCompletedOrderWithoutCost() {
        Order order = completedOrder(3L, 0, LocalDateTime.of(2026, 11, 8, 10, 0));
        order.setOrderCost(null); // суммы нет
        when(orderRepository.findByDeliveryDateGreaterThanEqualAndDeliveryDateLessThan(
                 any(), any())).thenReturn(List.of(order));
        when(operationRepository.findBySourceTypeAndOrderIdIn(any(), anyCollection())).thenReturn(List.of());
        when(operationRepository.findBySourceTypeAndOperationDateBetween(any(), any(), any())).thenReturn(List.of());

        service.syncCompletedOrders(START, END);

        verify(operationRepository, never()).save(any());
    }

    @Test
    void manualOperationCannotUseClientOrderCategory() {
        var dto = new com.nikulshin.detailing.model.dto.paymentcalendar.PcOperationSaveDto(
                LocalDate.of(2026, 11, 5),
                PCDirection.INCOME,
                PCCategory.CLIENT_ORDER, // запрещено для ручных операций
                FinanceType.CASH,
                new BigDecimal("100"),
                "попытка дубля");

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> service.createOperation(dto));
        verify(operationRepository, never()).save(any());
    }

    @Test
    void manualOperationDirectionMustMatchCategory() {
        var dto = new com.nikulshin.detailing.model.dto.paymentcalendar.PcOperationSaveDto(
                LocalDate.of(2026, 11, 5),
                PCDirection.INCOME,   // неверное направление для расходной статьи
                PCCategory.RENT,
                FinanceType.CURRENT_ACCOUNT,
                new BigDecimal("300"),
                null);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> service.createOperation(dto));
        verify(operationRepository, never()).save(any());
    }

    @Test
    void autoOperationCannotBeDeleted() {
        PaymentCalendarOperation auto = new PaymentCalendarOperation();
        auto.setId(10L);
        auto.setSourceType(PCSourceType.ORDER_AUTO);
        when(operationRepository.findById(10L)).thenReturn(Optional.of(auto));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> service.deleteOperation(10L));
        verify(operationRepository, never()).delete(any());
    }

    @Test
    void resolveOpeningBalanceUsesExplicitBalancePlusNetFlow() {
        com.nikulshin.detailing.model.domain.PaymentCalendarBalance explicit =
                new com.nikulshin.detailing.model.domain.PaymentCalendarBalance();
        explicit.setStartingBalance(new BigDecimal("1000"));
        when(balanceRepository.findByYearAndMonthAndAccountId(2026, 11, FinanceType.CASH))
                .thenReturn(Optional.of(explicit));
        // поток с 1 ноября до 5 ноября (не включая): +300
        when(operationRepository.netBetween(eq(FinanceType.CASH),
                eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 5))))
                .thenReturn(new BigDecimal("300"));

        BigDecimal opening = service.resolveOpeningBalance(LocalDate.of(2026, 11, 5), FinanceType.CASH);

        assertThat(opening).isEqualByComparingTo("1300");
    }

    @Test
    void resolveOpeningBalanceWalksBackToNearestExplicitMonth() {
        // Явного баланса в ноябре нет — поиск уходит в октябрь
        when(balanceRepository.findByYearAndMonthAndAccountId(2026, 11, FinanceType.CASH))
                .thenReturn(Optional.empty());
        com.nikulshin.detailing.model.domain.PaymentCalendarBalance october =
                new com.nikulshin.detailing.model.domain.PaymentCalendarBalance();
        october.setStartingBalance(new BigDecimal("500"));
        when(balanceRepository.findByYearAndMonthAndAccountId(2026, 10, FinanceType.CASH))
                .thenReturn(Optional.of(october));
        // поток с 1 октября (якорь — начало месяца явного баланса) до 5 ноября: +150
        when(operationRepository.netBetween(eq(FinanceType.CASH),
                eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 11, 5))))
                .thenReturn(new BigDecimal("150"));

        BigDecimal opening = service.resolveOpeningBalance(LocalDate.of(2026, 11, 5), FinanceType.CASH);

        assertThat(opening).isEqualByComparingTo("650");
    }

    @Test
    void resolveOpeningBalanceFallsBackToAllOperationsNet() {
        when(balanceRepository.findByYearAndMonthAndAccountId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(operationRepository.netBetween(eq(FinanceType.CASH),
                eq(LocalDate.of(2000, 1, 1)), eq(LocalDate.of(2026, 11, 1))))
                .thenReturn(new BigDecimal("-250"));

        BigDecimal opening = service.resolveOpeningBalance(LocalDate.of(2026, 11, 1), FinanceType.CASH);

        assertThat(opening).isEqualByComparingTo("-250");
    }
}
