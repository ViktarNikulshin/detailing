package com.nikulshin.detailing.model.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Описывает полный детальный отчет по одному мастеру.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDetailReportDto {

    @JsonProperty("masterId")
    private Integer masterId;

    @JsonProperty("masterFirstName")
    private String masterFirstName;

    @JsonProperty("masterLastName")
    private String masterLastName;

    @JsonProperty("reportDetails")
    private List<MasterDetailEarningDto> reportDetails;
}