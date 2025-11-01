package com.nikulshin.detailing.configuration;

import com.nikulshin.detailing.model.domain.Role;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.repository.RoleRepository;
import com.nikulshin.detailing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args)  {
        Role adminRole = createRoleIfNotFound("ADMIN");
        Role managerRole = createRoleIfNotFound("MANAGER");
        Role masterRole = createRoleIfNotFound("MASTER");

        createAdminUserIfNotFound(Set.of(adminRole, managerRole));

        // 3. (Опционально) Инициализация справочников
        // ... ваш код для 'SomeDictionaryRepository'
    }

    private Role createRoleIfNotFound(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
    }

    private void createAdminUserIfNotFound(Set<Role> roles) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("password"));
            adminUser.setRoles(roles);
            adminUser.setPhone("+375445419227");
            adminUser.setEnabled(true);

            userRepository.save(adminUser);
            System.out.println("Admin user created!");
        }
    }
}
