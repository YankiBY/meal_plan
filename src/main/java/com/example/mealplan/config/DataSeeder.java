package com.example.mealplan.config;

import com.example.mealplan.entity.Role;
import com.example.mealplan.entity.User;
import com.example.mealplan.repository.RoleRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner seedAdminUser() {
        return args -> {
            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

            userRepository.findByUsername("admin").ifPresentOrElse(u -> {
                // keep existing
            }, () -> {
                Set<Role> roles = new HashSet<>();
                roles.add(roleUser);
                roles.add(roleAdmin);
                User admin = User.builder()
                        .username("admin")
                        .email("admin@local")
                        .password(passwordEncoder.encode("admin"))
                        .blocked(false)
                        .roles(roles)
                        .build();
                userRepository.save(admin);
                log.info("Seeded default admin user: admin/admin");
            });
        };
    }
}

