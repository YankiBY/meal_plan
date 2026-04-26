package com.example.mealplan.controller;

import com.example.mealplan.dto.HealthProfileDto;
import com.example.mealplan.entity.HealthProfile;
import com.example.mealplan.service.HealthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final HealthProfileService healthProfileService;

    @PutMapping
    public ResponseEntity<HealthProfile> updateProfile(Authentication authentication, @RequestBody HealthProfileDto dto) {
        return ResponseEntity.ok(healthProfileService.updateProfile(authentication.getName(), dto));
    }
}
