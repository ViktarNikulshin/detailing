package com.nikulshin.detailing.model.dto.report;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeProductivityRecordDto {
    private Long id;
    private Long employeeId;
    private LocalDate date;
    private Long workTypeId;
    private String workTypeName;
    private BigDecimal checkAmount;
    private BigDecimal usnAmount;
    private BigDecimal salary;
    private BigDecimal taxesAmount;
    private BigDecimal rentAmount;
    private BigDecimal materialsAmount;
    private BigDecimal profit;
}
