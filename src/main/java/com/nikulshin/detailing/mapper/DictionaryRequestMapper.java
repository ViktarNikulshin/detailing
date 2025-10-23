package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.dto.DictionaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DictionaryRequestMapper extends BaseMapper<Dictionary, DictionaryDto> {

    @Override
    @Mapping(source = "active", target = "isActive")
    Dictionary dtoToDomain(DictionaryDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "active", target = "isActive")
    void updateEntityFromRequest(DictionaryDto request, @MappingTarget Dictionary dictionary);
}