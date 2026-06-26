package com.nikulshin.detailing.model.dto.report;

import java.math.BigDecimal;

public record UpdateMetaRequest(
        int year,
        int month,
        BigDecimal previousBalance,
        BigDecimal additionalAgreement
) {
}
