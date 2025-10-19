package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.WorkMasterAssignment;
import com.nikulshin.detailing.model.dto.MasterAssignmentDto;
import com.nikulshin.detailing.model.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MasterAssignmentMapper extends BaseMapper<WorkMasterAssignment, MasterAssignmentDto> {
    @Mapping(target = "master", ignore = true)
    WorkMasterAssignment dtoToDomain(OrderDto MasterAssignmentDto);

}