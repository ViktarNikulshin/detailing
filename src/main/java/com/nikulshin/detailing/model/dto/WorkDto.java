package com.nikulshin.detailing.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkDto {
    private Long id;
    private String comment;
    private DictionaryDto workType;
    private List<MasterAssignmentDto> assignments; // UPDATED: Список назначений
    private Integer cost; // Стоимость работы
    private List<DictionaryDto> parts;
}