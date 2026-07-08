package com.nikulshin.detailing.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkDto {
    private Long id;
    private String comment;
    private DictionaryDto workType;
    private Integer cost; // Стоимость работы
}