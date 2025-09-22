package com.nikulshin.detailing.service;


import com.nikulshin.detailing.mapper.UserMapper;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.AuthRequest;
import com.nikulshin.detailing.model.dto.AuthResponse;
import com.nikulshin.detailing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public AuthResponse authenticate(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsernameWithRoles(authRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        String token = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(token, userMapper.domainToDto(user));
    }

    public boolean checkToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}