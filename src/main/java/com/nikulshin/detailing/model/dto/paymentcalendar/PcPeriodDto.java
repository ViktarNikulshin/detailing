package com.nikulshin.detailing.model.dto.paymentcalendar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Ответ платёжного календаря за период (месяц или неделя). */
public record PcPeriodDto(
        String mode,                 // MONTH / WEEK
        LocalDate periodStart,
        LocalDate periodEnd,
        List<PcBalanceDto> balances,             // Остатки по счетам
        List<PcDayDto> days,                     // Дни периода
        PcCategoryTotals incomesTotal,           // Итого поступлений за период
        PcPaymentTotals paymentsTotal,           // Итого платежей за период
        BigDecimal totalOpening,                 // Общий остаток на начало периода
        BigDecimal totalClosing,                 // Общий остаток на конец периода
        boolean hasCashGap                       // Был ли кассовый разрыв в периоде
) {
}
