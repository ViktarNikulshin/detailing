package com.nikulshin.detailing.model.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LandlordRecordDto(
        Long id,
        LocalDate date,
        String carModel,
        String workTypeName,
        BigDecimal cost
) {
}