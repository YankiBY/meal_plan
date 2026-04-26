package com.example.mealplan.service;

import com.example.mealplan.dto.RegistrationRequest;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.User;
import com.example.mealplan.entity.Role;
import com.example.mealplan.mapper.UserMapper;
import com.example.mealplan.repository.RoleRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto register(RegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .roles(new java.util.HashSet<>(java.util.List.of(userRole)))
            .build();

        return userMapper.toDto(userRepository.save(user));
    }

    public UserDto getByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(userMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
