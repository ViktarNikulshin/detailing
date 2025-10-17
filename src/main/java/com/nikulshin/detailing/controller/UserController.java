package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.RoleDto;
import com.nikulshin.detailing.model.dto.UserDto;
import com.nikulshin.detailing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        userService.updateUserInfo(id, userDto);
        return ResponseEntity.ok(userDto);
    }
    @PostMapping()
    public ResponseEntity<String> createUser(@RequestBody UserDto userDto) {
        userService.createUser(userDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/role/{code}")
    public List<UserDto> findByRole(@PathVariable String code) {
        return userService.findByRole(code);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> getAllRole() {
        return ResponseEntity.ok(userService.getAllRole());
    }

    @GetMapping("/updateRoles/{id}")
    public ResponseEntity<UserDto> updateRoles(@PathVariable Long id, @RequestParam List<Long> roleIds) {
        return ResponseEntity.ok(userService.updateUserRoles(id, roleIds));
    }

    @GetMapping("/changePassword/{username}")
    public ResponseEntity<String> changePassword(@PathVariable String username, @RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(username, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }
}
