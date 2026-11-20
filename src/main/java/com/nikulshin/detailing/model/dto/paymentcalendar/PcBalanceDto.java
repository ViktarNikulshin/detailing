package com.nikulshin.detailing.model.dto.paymentcalendar;

import java.math.BigDecimal;

/** Остатки по счёту за период: входящий, чистый поток, исходящий. */
public record PcBalanceDto(
        String account,      // CASH / NON_CASH / CURRENT_ACCOUNT
        BigDecimal opening,  // Остаток на начало периода
        BigDecimal net,      // Чистый поток за период
        BigDecimal closing   // Остаток на конец периода
) {
}
