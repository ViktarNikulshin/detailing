package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.UserDto;
import com.nikulshin.detailing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        userService.updateUserInfo(id, userDto);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/role/{code}")
    public List<UserDto> findByRole(@PathVariable String code) {
        return userService.findByRole(code);
    }

    @GetMapping("/changePassword/{username}")
    public ResponseEntity<String> changePassword(@PathVariable String username, @RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(username, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }
}
