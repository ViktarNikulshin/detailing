package com.nikulshin.detailing.service;

import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public List<User> findAll() {
        return userRepository.findAll();
    }
    public List<User> findByRole(String code) {
        return userRepository.findAllByRole(code);
    }
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
