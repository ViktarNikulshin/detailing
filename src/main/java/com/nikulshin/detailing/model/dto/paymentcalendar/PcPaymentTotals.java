package com.nikulshin.detailing.model.dto.paymentcalendar;

import java.math.BigDecimal;

/** Итоги платежей (расходов) за день/период. */
public record PcPaymentTotals(
        BigDecimal rent,        // Аренда
        BigDecimal utilities,   // Коммунальные платежи
        BigDecimal salary,      // Зарплата (ФОТ)
        BigDecimal materials,   // Материалы и химия
        BigDecimal taxes,       // Налоги
        BigDecimal acquiring,   // Эквайринг / комиссии
        BigDecimal other,       // Прочие расходы
        BigDecimal total        // Итого платежей
) {
    public static PcPaymentTotals zero() {
        return new PcPaymentTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
