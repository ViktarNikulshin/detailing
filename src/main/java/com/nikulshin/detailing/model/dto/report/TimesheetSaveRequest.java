package com.nikulshin.detailing.model.dto.report;

import java.util.List;

public record TimesheetSaveRequest(
        Long masterId,
        String month, // Приходит в формате "YYYY-MM"
        List<TimesheetDayDto> records
) {
}