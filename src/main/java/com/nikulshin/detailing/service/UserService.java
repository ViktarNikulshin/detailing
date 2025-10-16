package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.UserMapper;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.UserDto;
import com.nikulshin.detailing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAll() {
        return userMapper.domainsToDtos(userRepository.findAll());
    }

    public List<UserDto> findByRole(String code) {
        return userMapper.domainsToDtos(userRepository.findAllByRole(code));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username); // Предполагается, что такой метод есть в репозитории
    }

    public List<User> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Transactional
    public void updateUserInfo(Long id, UserDto userDto) {
        userRepository.findById(id).ifPresent(u -> {
            u.setFirstName(userDto.getFirstName());
            u.setLastName(userDto.getLastName());
            userRepository.save(u);
        });
    }

    /**
     * Метод для смены пароля пользователя.
     *
     * @param username    Имя пользователя, для которого меняется пароль.
     * @param oldPassword Текущий пароль для проверки.
     * @param newPassword Новый пароль.
     * @throws RuntimeException если пользователь не найден или старый пароль неверен.
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        // Находим пользователя по имени
        User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        // Проверяем, совпадает ли предоставленный старый пароль с сохраненным
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }

        // Хэшируем новый пароль и сохраняем пользователя
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}