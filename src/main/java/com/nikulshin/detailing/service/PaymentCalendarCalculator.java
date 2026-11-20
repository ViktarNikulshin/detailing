package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.PCCategory;
import com.nikulshin.detailing.model.domain.PCDirection;
import com.nikulshin.detailing.model.domain.PaymentCalendarOperation;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcCategoryTotals;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcDayDto;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcPaymentTotals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Чистый расчётный движок платёжного календаря: агрегация операций по дням,
 * дневной баланс, кумулятивный остаток и индикатор кассового разрыва.
 * Не зависит от Spring и БД — легко покрывается unit-тестами.
 */
public final class PaymentCalendarCalculator {

    private PaymentCalendarCalculator() {
    }

    /**
     * Строит дневную сетку периода [start, end] включительно.
     *
     * @param openingTotal суммарный входящий остаток по всем счетам на начало периода
     */
    public static List<PcDayDto> buildDays(LocalDate start,
                                           LocalDate end,
                                           Map<LocalDate, List<PaymentCalendarOperation>> operationsByDate,
                                           BigDecimal openingTotal) {
        List<PcDayDto> days = new ArrayList<>();
        BigDecimal cumulative = nz(openingTotal);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<PaymentCalendarOperation> dayOps = operationsByDate.getOrDefault(date, List.of());

            PcCategoryTotals incomes = PcCategoryTotals.zero();
            PcPaymentTotals payments = PcPaymentTotals.zero();

            for (PaymentCalendarOperation op : dayOps) {
                if (op.getDirection() == PCDirection.INCOME) {
                    incomes = addToIncomes(incomes, op);
                } else {
                    payments = addToPayments(payments, op);
                }
            }

            incomes = withIncomesTotal(incomes);
            payments = withPaymentsTotal(payments);

            BigDecimal dayBalance = incomes.total().subtract(payments.total());
            cumulative = cumulative.add(dayBalance);
            boolean cashGap = cumulative.compareTo(BigDecimal.ZERO) < 0;

            days.add(new PcDayDto(date, incomes, payments, dayBalance, cumulative, cashGap));
        }
        return days;
    }

    /** Итоги поступлений и платежей по списку операций. */
    public static Totals totals(List<PaymentCalendarOperation> operations) {
        PcCategoryTotals incomes = PcCategoryTotals.zero();
        PcPaymentTotals payments = PcPaymentTotals.zero();

        for (PaymentCalendarOperation op : operations) {
            if (op.getDirection() == PCDirection.INCOME) {
                incomes = addToIncomes(incomes, op);
            } else {
                payments = addToPayments(payments, op);
            }
        }
        return new Totals(withIncomesTotal(incomes), withPaymentsTotal(payments));
    }

    private static PcCategoryTotals addToIncomes(PcCategoryTotals t, PaymentCalendarOperation op) {
        BigDecimal amount = nz(op.getAmount());
        if (op.getCategory() == PCCategory.CLIENT_ORDER) {
            return new PcCategoryTotals(t.orders().add(amount), t.other(), t.total());
        }
        return new PcCategoryTotals(t.orders(), t.other().add(amount), t.total());
    }

    private static PcPaymentTotals addToPayments(PcPaymentTotals t, PaymentCalendarOperation op) {
        BigDecimal amount = nz(op.getAmount());
        return switch (op.getCategory()) {
            case RENT -> new PcPaymentTotals(t.rent().add(amount), t.utilities(), t.salary(), t.materials(),
                    t.taxes(), t.acquiring(), t.other(), t.total());
            case UTILITIES -> new PcPaymentTotals(t.rent(), t.utilities().add(amount), t.salary(), t.materials(),
                    t.taxes(), t.acquiring(), t.other(), t.total());
            case SALARY -> new PcPaymentTotals(t.rent(), t.utilities(), t.salary().add(amount), t.materials(),
                    t.taxes(), t.acquiring(), t.other(), t.total());
            case MATERIALS -> new PcPaymentTotals(t.rent(), t.utilities(), t.salary(), t.materials().add(amount),
                    t.taxes(), t.acquiring(), t.other(), t.total());
            case TAXES -> new PcPaymentTotals(t.rent(), t.utilities(), t.salary(), t.materials(),
                    t.taxes().add(amount), t.acquiring(), t.other(), t.total());
            case ACQUIRING -> new PcPaymentTotals(t.rent(), t.utilities(), t.salary(), t.materials(),
                    t.taxes(), t.acquiring().add(amount), t.other(), t.total());
            default -> new PcPaymentTotals(t.rent(), t.utilities(), t.salary(), t.materials(),
                    t.taxes(), t.acquiring(), t.other().add(amount), t.total());
        };
    }

    private static PcCategoryTotals withIncomesTotal(PcCategoryTotals t) {
        return new PcCategoryTotals(scale(t.orders()), scale(t.other()), scale(t.orders().add(t.other())));
    }

    private static PcPaymentTotals withPaymentsTotal(PcPaymentTotals t) {
        BigDecimal total = t.rent().add(t.utilities()).add(t.salary()).add(t.materials())
                .add(t.taxes()).add(t.acquiring()).add(t.other());
        return new PcPaymentTotals(scale(t.rent()), scale(t.utilities()), scale(t.salary()), scale(t.materials()),
                scale(t.taxes()), scale(t.acquiring()), scale(t.other()), scale(total));
    }

    public static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public static BigDecimal scale(BigDecimal value) {
        return nz(value).setScale(2, RoundingMode.HALF_UP);
    }

    /** Группировка операций по дате операции. */
    public static Map<LocalDate, List<PaymentCalendarOperation>> groupByDate(List<PaymentCalendarOperation> operations) {
        return operations.stream().collect(Collectors.groupingBy(PaymentCalendarOperation::getOperationDate));
    }

    public record Totals(PcCategoryTotals incomes, PcPaymentTotals payments) {
    }
}
