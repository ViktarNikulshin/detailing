package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/role/{code}")
    public List<User> findByRole(@PathVariable String code) {
        return userService.findByRole(code);
    }

}
