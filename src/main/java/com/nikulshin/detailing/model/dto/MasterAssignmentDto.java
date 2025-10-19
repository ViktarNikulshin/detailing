package com.nikulshin.detailing.model.dto;

import lombok.Data;

@Data
public class MasterAssignmentDto {
    private Long id;
    private UserDto master;
    private Integer salaryPercent;
}