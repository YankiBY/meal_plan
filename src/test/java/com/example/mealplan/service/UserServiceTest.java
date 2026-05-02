package com.example.mealplan.service;

import com.example.mealplan.dto.RegistrationRequest;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.Role;
import com.example.mealplan.entity.User;
import com.example.mealplan.exception.BadRequestException;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.mapper.UserMapper;
import com.example.mealplan.repository.RoleRepository;
import com.example.mealplan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("ROLE_USER").build();
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded_password")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@test.com");
        testUserDto.setRoles(Set.of("ROLE_USER"));
    }

    @Test
    void register_success() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("password123");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        UserDto dto = new UserDto();
        dto.setUsername("newuser");
        when(userMapper.toDto(any(User.class))).thenReturn(dto);

        UserDto result = userService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throws() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("other@test.com");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throws() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("otheruser");
        request.setEmail("test@test.com");
        request.setPassword("password123");

        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_noRole_createsRole() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("password123");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(userRole);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        userService.register(request);

        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void getByUsername_found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.getByUsername("testuser");

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getByUsername_notFound_throws() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("nobody"));
    }

    @Test
    void findByUsername_found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = userService.findByUsername("testuser");

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void updateProfile_changesEmail() {
        UserDto updateDto = new UserDto();
        updateDto.setEmail("newemail@test.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("newemail@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        UserDto result = userService.updateProfile("testuser", updateDto);

        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_duplicateEmail_throws() {
        User otherUser = User.builder().id(2L).username("other").email("other@test.com").build();
        UserDto updateDto = new UserDto();
        updateDto.setEmail("other@test.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));

        assertThrows(BadRequestException.class, () -> userService.updateProfile("testuser", updateDto));
    }
}
