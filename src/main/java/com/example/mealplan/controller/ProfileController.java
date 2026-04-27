package com.example.mealplan.controller;

import com.example.mealplan.dto.HealthProfileDto;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.service.HealthProfileService;
import com.example.mealplan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final HealthProfileService healthProfileService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getByUsername(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<UserDto> updateProfile(Authentication authentication, @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.updateProfile(authentication.getName(), dto));
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserDto> uploadAvatar(Authentication authentication,
                                                @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(authentication.getName(), file));
    }

    @GetMapping("/health")
    public ResponseEntity<HealthProfileDto> getHealthProfile(Authentication authentication) {
        return ResponseEntity.ok(healthProfileService.getProfile(authentication.getName()));
    }

    @PutMapping("/health")
    public ResponseEntity<HealthProfileDto> updateHealthProfile(Authentication authentication,
                                                                 @RequestBody HealthProfileDto dto) {
        return ResponseEntity.ok(healthProfileService.updateProfile(authentication.getName(), dto));
    }
}
