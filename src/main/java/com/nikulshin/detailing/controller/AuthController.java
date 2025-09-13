package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.model.dto.AuthRequest;
import com.nikulshin.detailing.model.dto.AuthResponse;
import com.nikulshin.detailing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.authenticate(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken() {
        // Токен валидируется через JwtAuthenticationFilter
        return ResponseEntity.ok(true);
    }
}