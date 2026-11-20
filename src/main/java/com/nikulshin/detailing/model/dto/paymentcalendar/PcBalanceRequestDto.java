package com.nikulshin.detailing.model.dto.paymentcalendar;

import com.nikulshin.detailing.model.domain.FinanceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Сохранение стартового остатка платёжного календаря на месяц по счёту. */
public record PcBalanceRequestDto(
        @NotNull @Min(2000) @Max(2100) Integer year,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull FinanceType account,
        @NotNull BigDecimal amount
) {
}
