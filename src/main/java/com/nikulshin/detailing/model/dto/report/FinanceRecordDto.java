package com.nikulshin.detailing.model.dto.report;

import com.nikulshin.detailing.model.FinanceType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinanceRecordDto {
    private Long id;
    private LocalDate date;
    private BigDecimal income;
    private BigDecimal expUtilities;
    private BigDecimal expRent;
    private BigDecimal expMaterials;
    private BigDecimal expCredit;
    private BigDecimal expSalary;
    private BigDecimal expTaxes;
    private BigDecimal expOther;
    private FinanceType financeType;
}