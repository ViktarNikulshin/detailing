package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper extends BaseMapper <User, UserDto> {

}
