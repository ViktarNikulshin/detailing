package com.nikulshin.detailing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String type;
    private Boolean active;
    private List<String> tags;
    private List<DictionaryDto> parts;
}
