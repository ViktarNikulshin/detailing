package com.nikulshin.detailing.model.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate; // Рекомендуется использовать LocalDate для даты без времени

/**
 * Описывает заработок по конкретному заказу для одного типа работ.
 * Используется в детальном отчете.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEarningDto {

    @JsonProperty("orderId")
    private Integer orderId;

    @JsonProperty("clientName")
    private String clientName;

    // Используем String для соответствия "ISO-строке" или LocalDate/LocalDateTime
    // Если в ISO-строке только дата (например, "2024-05-20"), используйте LocalDate
    @JsonProperty("executionDate")
    private String executionDate;

    @JsonProperty("earning")
    private Double earning;
}