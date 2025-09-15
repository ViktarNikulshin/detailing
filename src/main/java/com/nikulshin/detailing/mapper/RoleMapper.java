package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.Role;
import com.nikulshin.detailing.model.dto.RoleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper extends BaseMapper <Role, RoleDto> {

}
