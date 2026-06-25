package com.nikulshin.detailing.model.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record UserTimesheetDto(
        BigDecimal previousBalance,
        BigDecimal interimPayments,
        List<TimesheetDayDto> records
) {
}