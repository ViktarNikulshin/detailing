package com.nikulshin.detailing.model.dto.report;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinanceBalanceRequest {
    private Integer year;
    private Integer month;
    private BigDecimal amount;
    private String type;
}