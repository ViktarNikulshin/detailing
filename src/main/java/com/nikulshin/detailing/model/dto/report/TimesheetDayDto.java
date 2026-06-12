package com.nikulshin.detailing.model.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

// Один день в табеле
public record TimesheetDayDto(
        LocalDate date,
        boolean isAbsent,
        BigDecimal contentRub,
        BigDecimal salaryRub
) {
}

