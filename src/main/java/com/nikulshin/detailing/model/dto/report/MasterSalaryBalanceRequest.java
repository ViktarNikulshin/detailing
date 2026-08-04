package com.nikulshin.detailing.model.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class MasterSalaryBalanceRequest {
    private BigDecimal previousBalance;
    private BigDecimal interimPayments;
    private Integer month;
    private Integer year;
    private Long masterId;
    private Boolean isTimesheet;
}
