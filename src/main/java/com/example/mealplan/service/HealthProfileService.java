package com.example.mealplan.service;

import com.example.mealplan.dto.HealthProfileDto;
import com.example.mealplan.entity.HealthProfile;
import com.example.mealplan.entity.User;
import com.example.mealplan.repository.HealthProfileRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthProfileService {
    private final HealthProfileRepository healthProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public HealthProfile updateProfile(String username, HealthProfileDto dto) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthProfile profile = healthProfileRepository.findByUserId(user.getId())
            .orElse(HealthProfile.builder().user(user).build());
        
        profile.setWeight(dto.getWeight());
        profile.setHeight(dto.getHeight());
        profile.setAge(dto.getAge());
        profile.setGender(dto.getGender());
        profile.setActivityLevel(dto.getActivityLevel());
        
        // Basic calorie calculation (Mifflin-St Jeor)
        double bmr;
        if ("MALE".equalsIgnoreCase(dto.getGender())) {
            bmr = 10 * dto.getWeight() + 6.25 * dto.getHeight() - 5 * dto.getAge() + 5;
        } else {
            bmr = 10 * dto.getWeight() + 6.25 * dto.getHeight() - 5 * dto.getAge() - 161;
        }
        
        double multiplier = switch (dto.getActivityLevel().toUpperCase()) {
            case "SEDENTARY" -> 1.2;
            case "LIGHT" -> 1.375;
            case "MODERATE" -> 1.55;
            case "ACTIVE" -> 1.725;
            case "VERY_ACTIVE" -> 1.9;
            default -> 1.2;
        };
        
        profile.setDailyCalorieTarget(bmr * multiplier);
        // Simple macros ratio: 30% protein, 30% fat, 40% carbs
        profile.setDailyProteinTarget((profile.getDailyCalorieTarget() * 0.3) / 4);
        profile.setDailyFatTarget((profile.getDailyCalorieTarget() * 0.3) / 9);
        profile.setDailyCarbTarget((profile.getDailyCalorieTarget() * 0.4) / 4);
        
        return healthProfileRepository.save(profile);
    }
}
