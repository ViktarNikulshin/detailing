package com.nikulshin.detailing.model.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasterSalaryRecordDto {
    private Long id;
    private Long masterId;
    private String workTypeName;
    private LocalDateTime date;
    private String carModel;
    private Integer salary;
}
