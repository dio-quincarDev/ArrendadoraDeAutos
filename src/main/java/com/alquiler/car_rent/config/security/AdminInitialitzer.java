package com.alquiler.car_rent.config.security;

import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.repositories.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitialitzer implements CommandLineRunner {
    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    public void run(String... args) throws Exception {
        if (!userEntityRepository.existsByRole(Role.SUPER_ADMIN)) {
            log.debug("No SUPER_ADMIN user found. Creating initial super admin.");

            String username = env.getProperty("application.initial-admin.username");
            String password = env.getProperty("application.initial-admin.password");
            String email = env.getProperty("application.initial-admin.email");

            if (username == null || password == null || email == null) {
                log.error("Initial super admin user properties (username, password, email) are not fully configured. Skipping creation.");
                return;
            }

            UserEntity admin = UserEntity.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .role(Role.SUPER_ADMIN)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userEntityRepository.save(admin);
            log.info("Initial SUPER_ADMIN user '{}' created successfully.", username);

        } else {
            log.info("SUPER_ADMIN user already exists. Skipping creation.");
        }
    }
}
