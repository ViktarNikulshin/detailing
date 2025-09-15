package com.nikulshin.detailing.model.dto;

import com.nikulshin.detailing.model.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;
}