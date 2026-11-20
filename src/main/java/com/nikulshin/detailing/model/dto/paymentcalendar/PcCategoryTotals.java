package com.nikulshin.detailing.model.dto.paymentcalendar;

import java.math.BigDecimal;

/** Итоги поступлений за день/период. */
public record PcCategoryTotals(
        BigDecimal orders,   // Заказы (по факту завершения)
        BigDecimal other,    // Прочие поступления
        BigDecimal total     // Итого поступлений
) {
    public static PcCategoryTotals zero() {
        return new PcCategoryTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
