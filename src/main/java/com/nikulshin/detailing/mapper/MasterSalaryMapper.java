package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.MasterSalaryLog;
import com.nikulshin.detailing.model.dto.report.MasterSalaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DictionaryMapper.class, UserMapper.class})
public interface MasterSalaryMapper extends BaseMapper<MasterSalaryLog, MasterSalaryDto> {
    @Mapping(target = "masterId", source = "master.id")
    @Mapping(target = "workTypeId", source = "workType.id")
    @Mapping(target = "workTypeName", source = "workType.name")
    @Override
    MasterSalaryDto domainToDto(MasterSalaryLog entity);

}
