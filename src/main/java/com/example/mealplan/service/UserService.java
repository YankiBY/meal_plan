package com.example.mealplan.service;

import com.example.mealplan.dto.RegistrationRequest;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.User;
import com.example.mealplan.entity.Role;
import com.example.mealplan.exception.BadRequestException;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.mapper.UserMapper;
import com.example.mealplan.repository.RoleRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional
    public UserDto register(RegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Пользователь с таким именем уже существует");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Пользователь с таким email уже существует");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .roles(new HashSet<>(List.of(userRole)))
            .build();

        return userMapper.toDto(userRepository.save(user));
    }

    public UserDto getByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(userMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public UserDto uploadAvatar(String username, MultipartFile file) {
        User user = findByUsername(username);

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            user.setAvatarPath("/uploads/" + fileName);
            userRepository.save(user);

            return userMapper.toDto(user);
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось загрузить файл аватара", ex);
        }
    }

    @Transactional
    public UserDto updateProfile(String username, UserDto dto) {
        User user = findByUsername(username);
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BadRequestException("Email уже используется");
            }
            user.setEmail(dto.getEmail());
        }
        return userMapper.toDto(userRepository.save(user));
    }
}
