package com.nikulshin.detailing.model.dto.paymentcalendar;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Один день платёжного календаря. */
public record PcDayDto(
        LocalDate date,
        PcCategoryTotals incomes,        // Поступления: заказы / прочие / итого
        PcPaymentTotals payments,        // Платежи: аренда, коммуналка, ФОТ, материалы, налоги, эквайринг, прочие, итого
        BigDecimal dayBalance,           // Дневной баланс = итого поступлений - итого платежей
        BigDecimal cumulativeBalance,    // Кумулятивный остаток на конец дня
        boolean cashGap                  // Индикатор кассового разрыва (кумулятивный остаток < 0)
) {
}
