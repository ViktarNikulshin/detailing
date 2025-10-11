package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.UserMapper;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.UserDto;
import com.nikulshin.detailing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> findAll() {
        return userMapper.domainsToDtos(userRepository.findAll());
    }

    public List<UserDto> findByRole(String code) {
        return userMapper.domainsToDtos(userRepository.findAllByRole(code));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    public void updateUser(Long id, UserDto userDto) {
        userRepository.findById(id).ifPresent(u -> {
            u.setFirstName(userDto.getFirstName());
            u.setLastName(userDto.getLastName());
            userRepository.save(u);
        });
    }
}
