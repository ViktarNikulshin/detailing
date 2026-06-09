package com.nikulshin.detailing.model.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasterSalaryDto {
    private Long id;
    private Long masterId;
    private Long workTypeId;
    private String workTypeName;
    private LocalDateTime date;
    private String carModel;
    private Integer salary;
}
