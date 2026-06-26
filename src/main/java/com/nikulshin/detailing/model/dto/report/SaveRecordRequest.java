package com.nikulshin.detailing.model.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveRecordRequest(
        Long id, // Передается null при создании новой строки
        int year,
        int month,
        LocalDate date,
        String carModel,
        String workTypeName,
        BigDecimal cost
) {}
