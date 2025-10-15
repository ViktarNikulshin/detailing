package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.Work;
import com.nikulshin.detailing.model.dto.WorkDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {DictionaryMapper.class})
public interface WorkMapper extends BaseMapper <Work, WorkDto> {

}
