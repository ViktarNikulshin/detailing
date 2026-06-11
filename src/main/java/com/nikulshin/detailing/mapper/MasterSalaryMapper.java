package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.MasterSalaryLog;
import com.nikulshin.detailing.model.dto.report.MasterSalaryRecordDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DictionaryMapper.class, UserMapper.class})
public interface MasterSalaryMapper extends BaseMapper<MasterSalaryLog, MasterSalaryRecordDto> {
    @Mapping(target = "masterId", source = "master.id")
    @Override
    MasterSalaryRecordDto domainToDto(MasterSalaryLog entity);

}
