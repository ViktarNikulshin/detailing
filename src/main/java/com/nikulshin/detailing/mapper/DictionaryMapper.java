package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.Dictionary;
import com.nikulshin.detailing.model.dto.DictionaryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DictionaryMapper extends BaseMapper<Dictionary, DictionaryDto> {

}
