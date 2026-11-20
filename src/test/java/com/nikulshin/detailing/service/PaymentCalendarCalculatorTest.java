package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.FinanceType;
import com.nikulshin.detailing.model.domain.PCCategory;
import com.nikulshin.detailing.model.domain.PCDirection;
import com.nikulshin.detailing.model.domain.PCSourceType;
import com.nikulshin.detailing.model.domain.PaymentCalendarOperation;
import com.nikulshin.detailing.model.dto.paymentcalendar.PcDayDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты чистого расчётного движка платёжного календаря:
 * агрегация по дням, дневной баланс, кумулятивный остаток, кассовый разрыв.
 */
class PaymentCalendarCalculatorTest {

    private static final LocalDate D1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate D2 = LocalDate.of(2026, 11, 2);
    private static final LocalDate D3 = LocalDate.of(2026, 11, 3);

    private static PaymentCalendarOperation op(LocalDate date, PCDirection direction, PCCategory category,
                                               FinanceType account, String amount) {
        PaymentCalendarOperation o = new PaymentCalendarOperation();
        o.setOperationDate(date);
        o.setDirection(direction);
        o.setCategory(category);
        o.setAccountId(account);
        o.setAmount(new BigDecimal(amount));
        o.setSourceType(PCSourceType.MANUAL);
        return o;
    }

    @Test
    void buildsEveryDayOfRangeEvenWithoutOperations() {
        List<PcDayDto> days = PaymentCalendarCalculator.buildDays(
                D1, D3, Map.of(), new BigDecimal("100.00"));

        assertThat(days).hasSize(3);
        assertThat(days.get(0).date()).isEqualTo(D1);
        assertThat(days.get(2).date()).isEqualTo(D3);
        days.forEach(d -> {
            assertThat(d.incomes().total()).isEqualByComparingTo("0");
            assertThat(d.payments().total()).isEqualByComparingTo("0");
            assertThat(d.dayBalance()).isEqualByComparingTo("0");
            assertThat(d.cumulativeBalance()).isEqualByComparingTo("100");
            assertThat(d.cashGap()).isFalse();
        });
    }

    @Test
    void aggregatesIncomesAndPaymentsByCategoryPerDay() {
        List<PaymentCalendarOperation> ops = List.of(
                op(D1, PCDirection.INCOME, PCCategory.CLIENT_ORDER, FinanceType.CASH, "500"),
                op(D1, PCDirection.INCOME, PCCategory.OTHER_INCOME, FinanceType.CASH, "100"),
                op(D1, PCDirection.EXPENSE, PCCategory.RENT, FinanceType.CURRENT_ACCOUNT, "300"),
                op(D1, PCDirection.EXPENSE, PCCategory.UTILITIES, FinanceType.CURRENT_ACCOUNT, "50"),
                op(D2, PCDirection.EXPENSE, PCCategory.SALARY, FinanceType.CASH, "200"),
                op(D2, PCDirection.EXPENSE, PCCategory.MATERIALS, FinanceType.CASH, "30"),
                op(D2, PCDirection.EXPENSE, PCCategory.TAXES, FinanceType.NON_CASH, "20"),
                op(D2, PCDirection.EXPENSE, PCCategory.ACQUIRING, FinanceType.NON_CASH, "10"),
                op(D3, PCDirection.EXPENSE, PCCategory.OTHER_EXPENSE, FinanceType.CASH, "5")
        );

        List<PcDayDto> days = PaymentCalendarCalculator.buildDays(
                D1, D3, PaymentCalendarCalculator.groupByDate(ops), BigDecimal.ZERO);

        // День 1: поступления 600, платежи 350, баланс +250
        PcDayDto day1 = days.get(0);
        assertThat(day1.incomes().orders()).isEqualByComparingTo("500");
        assertThat(day1.incomes().other()).isEqualByComparingTo("100");
        assertThat(day1.incomes().total()).isEqualByComparingTo("600");
        assertThat(day1.payments().rent()).isEqualByComparingTo("300");
        assertThat(day1.payments().utilities()).isEqualByComparingTo("50");
        assertThat(day1.payments().total()).isEqualByComparingTo("350");
        assertThat(day1.dayBalance()).isEqualByComparingTo("250");
        assertThat(day1.cumulativeBalance()).isEqualByComparingTo("250");

        // День 2: платежи 260 (ФОТ 200 + материалы 30 + налоги 20 + эквайринг 10), баланс -260
        PcDayDto day2 = days.get(1);
        assertThat(day2.payments().salary()).isEqualByComparingTo("200");
        assertThat(day2.payments().materials()).isEqualByComparingTo("30");
        assertThat(day2.payments().taxes()).isEqualByComparingTo("20");
        assertThat(day2.payments().acquiring()).isEqualByComparingTo("10");
        assertThat(day2.payments().total()).isEqualByComparingTo("260");
        assertThat(day2.dayBalance()).isEqualByComparingTo("-260");
        assertThat(day2.cumulativeBalance()).isEqualByComparingTo("-10");
        assertThat(day2.cashGap()).isTrue(); // кассовый разрыв: кумулятив ушёл в минус

        // День 3: прочие 5, кумулятив -15, разрыв продолжается
        PcDayDto day3 = days.get(2);
        assertThat(day3.payments().other()).isEqualByComparingTo("5");
        assertThat(day3.cumulativeBalance()).isEqualByComparingTo("-15");
        assertThat(day3.cashGap()).isTrue();
    }

    @Test
    void cashGapDisappearsWhenCumulativeReturnsToPositive() {
        List<PaymentCalendarOperation> ops = List.of(
                op(D1, PCDirection.EXPENSE, PCCategory.RENT, FinanceType.CASH, "400"),
                op(D2, PCDirection.INCOME, PCCategory.CLIENT_ORDER, FinanceType.CASH, "900")
        );

        List<PcDayDto> days = PaymentCalendarCalculator.buildDays(
                D1, D2, PaymentCalendarCalculator.groupByDate(ops), BigDecimal.ZERO);

        assertThat(days.get(0).cashGap()).isTrue();
        assertThat(days.get(0).cumulativeBalance()).isEqualByComparingTo("-400");
        assertThat(days.get(1).cashGap()).isFalse();
        assertThat(days.get(1).cumulativeBalance()).isEqualByComparingTo("500");
    }

    @Test
    void totalsAggregatesWholePeriod() {
        List<PaymentCalendarOperation> ops = List.of(
                op(D1, PCDirection.INCOME, PCCategory.CLIENT_ORDER, FinanceType.CASH, "1000"),
                op(D1, PCDirection.INCOME, PCCategory.OTHER_INCOME, FinanceType.CASH, "200"),
                op(D1, PCDirection.EXPENSE, PCCategory.RENT, FinanceType.CURRENT_ACCOUNT, "700"),
                op(D2, PCDirection.EXPENSE, PCCategory.ACQUIRING, FinanceType.NON_CASH, "25")
        );

        PaymentCalendarCalculator.Totals totals = PaymentCalendarCalculator.totals(ops);

        assertThat(totals.incomes().orders()).isEqualByComparingTo("1000");
        assertThat(totals.incomes().other()).isEqualByComparingTo("200");
        assertThat(totals.incomes().total()).isEqualByComparingTo("1200");
        assertThat(totals.payments().rent()).isEqualByComparingTo("700");
        assertThat(totals.payments().acquiring()).isEqualByComparingTo("25");
        assertThat(totals.payments().total()).isEqualByComparingTo("725");
    }
}
