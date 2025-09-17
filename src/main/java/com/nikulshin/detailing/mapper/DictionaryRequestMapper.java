package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.dto.DictionaryDto;
import com.nikulshin.detailing.model.dto.DictionaryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DictionaryRequestMapper extends BaseMapper<Dictionary, DictionaryRequest>{

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(DictionaryRequest request, @MappingTarget Dictionary dictionary);
}
