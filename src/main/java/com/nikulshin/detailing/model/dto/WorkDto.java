package com.nikulshin.detailing.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkDto {
    private Integer id;
    private String comment;
    private DictionaryDto workType;
    private List<DictionaryDto> parts;
}
