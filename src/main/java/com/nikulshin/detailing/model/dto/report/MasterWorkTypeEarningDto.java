package com.nikulshin.detailing.model.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Описывает заработок одного мастера по одному типу работ.
 * Используется в общем отчете по всем мастерам.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterWorkTypeEarningDto {

    @JsonProperty("workTypeId")
    private Integer workTypeId;

    @JsonProperty("workTypeName")
    private String workTypeName;

    @JsonProperty("totalEarnings")
    private Double totalEarnings;
}