package com.nikulshin.detailing.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String phone;
    private String firstName;
    private String lastName;
    private List<RoleDto> roles;
}
