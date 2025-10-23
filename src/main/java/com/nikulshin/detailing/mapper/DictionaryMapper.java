package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.dto.DictionaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DictionaryMapper extends BaseMapper<Dictionary, DictionaryDto> {

    @Override
    @Mapping(source = "isActive", target = "active")
    DictionaryDto domainToDto(Dictionary domain);
}