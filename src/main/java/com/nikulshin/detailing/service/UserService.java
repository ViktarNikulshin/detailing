package com.nikulshin.detailing.service;

import com.nikulshin.detailing.mapper.RoleMapper;
import com.nikulshin.detailing.mapper.UserMapper;
import com.nikulshin.detailing.model.domain.Role;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.RoleDto;
import com.nikulshin.detailing.model.dto.UserDto;
import com.nikulshin.detailing.repository.RoleRepository;
import com.nikulshin.detailing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Value("${app.default-password}")
    private String defaultPassword;

    public List<UserDto> findAll() {
        return userMapper.domainsToDtos(userRepository.findAll());
    }

    public List<UserDto> findByRole(String code) {
        return userMapper.domainsToDtos(userRepository.findAllByRole(code));
    }

    public UserDto getUserById(Long id) {
        return userMapper.domainToDto(userRepository.findById(id).orElse(null));
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

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<RoleDto> getAllRole() {
        return roleMapper.domainsToDtos(roleRepository.findAll());
    }

    public UserDto updateUserRoles(Long id, List<Long> roleIds) {
        Set<Role> roles = roleRepository.findALLByIdIn(roleIds);
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with username: " + id));
        user.setRoles(roles);
       return userMapper.domainToDto( userRepository.save(user));
    }

    public void createUser(UserDto userDto) {
        User user = userMapper.dtoToDomain(userDto);
        user.setPassword(passwordEncoder.encode(defaultPassword));
        userRepository.save(user);
    }
}