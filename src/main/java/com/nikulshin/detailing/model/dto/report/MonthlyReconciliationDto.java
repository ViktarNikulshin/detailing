package com.nikulshin.detailing.model.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReconciliationDto(
        int year,
        int month,
        BigDecimal previousBalance,
        BigDecimal additionalAgreement,
        BigDecimal calculatedBalance,
        List<LandlordRecordDto> records
) {
}

