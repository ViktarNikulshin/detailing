package com.nikulshin.detailing.model.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Описывает детальный заработок по одному типу работ, с разбивкой по заказам.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDetailEarningDto {

    @JsonProperty("workTypeId")
    private Integer workTypeId;

    @JsonProperty("workTypeName")
    private String workTypeName;

    @JsonProperty("earningsByOrder")
    private List<OrderEarningDto> earningsByOrder;
}