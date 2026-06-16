package com.nikulshin.detailing.model.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class FinanceMonthSummaryDto {
    private BigDecimal startingBalance;
    private List<FinanceRecordDto> records;
}