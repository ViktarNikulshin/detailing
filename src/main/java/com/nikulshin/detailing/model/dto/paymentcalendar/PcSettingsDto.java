package com.nikulshin.detailing.model.dto.paymentcalendar;

import com.nikulshin.detailing.model.domain.FinanceType;
import jakarta.validation.constraints.NotNull;

/** Настройки платёжного календаря. */
public record PcSettingsDto(
        @NotNull FinanceType defaultIncomeAccount
) {
}
