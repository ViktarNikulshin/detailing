package com.nikulshin.detailing.model.dto.report;

import java.util.List;

public record UserTimesheetDto(
        Integer previousBalance,
        List<TimesheetDayDto> records
) {
}