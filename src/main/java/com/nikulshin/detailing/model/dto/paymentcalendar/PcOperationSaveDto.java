package com.nikulshin.detailing.model.dto.paymentcalendar;

import com.nikulshin.detailing.model.domain.FinanceType;
import com.nikulshin.detailing.model.domain.PCCategory;
import com.nikulshin.detailing.model.domain.PCDirection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Создание/редактирование ручной операции платёжного календаря. */
public record PcOperationSaveDto(
        @NotNull LocalDate date,
        @NotNull PCDirection direction,
        @NotNull PCCategory category,
        @NotNull FinanceType account,
        @NotNull @Positive BigDecimal amount,
        @Size(max = 500) String description
) {
}
