package com.example.mealplan.service;

import com.example.mealplan.dto.HealthProfileDto;
import com.example.mealplan.entity.Allergen;
import com.example.mealplan.entity.Disease;
import com.example.mealplan.entity.HealthProfile;
import com.example.mealplan.entity.User;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.repository.AllergenRepository;
import com.example.mealplan.repository.DiseaseRepository;
import com.example.mealplan.repository.HealthProfileRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthProfileService {
    private final HealthProfileRepository healthProfileRepository;
    private final UserRepository userRepository;
    private final DiseaseRepository diseaseRepository;
    private final AllergenRepository allergenRepository;

    public HealthProfileDto getProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        HealthProfile profile = healthProfileRepository.findByUserId(user.getId())
            .orElse(null);

        if (profile == null) {
            HealthProfileDto emptyDto = new HealthProfileDto();
            emptyDto.setDiseaseIds(new HashSet<>());
            emptyDto.setAllergenIds(new HashSet<>());
            emptyDto.setDiseaseNames(new HashSet<>());
            emptyDto.setAllergenNames(new HashSet<>());
            return emptyDto;
        }

        return toDto(profile);
    }

    @Transactional
    public HealthProfileDto updateProfile(String username, HealthProfileDto dto) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        HealthProfile profile = healthProfileRepository.findByUserId(user.getId())
            .orElse(HealthProfile.builder().user(user).build());

        profile.setWeight(dto.getWeight());
        profile.setHeight(dto.getHeight());
        profile.setAge(dto.getAge());
        profile.setGender(dto.getGender());
        profile.setActivityLevel(dto.getActivityLevel());

        if (dto.getDiseaseIds() != null) {
            Set<Disease> diseases = new HashSet<>(diseaseRepository.findAllById(dto.getDiseaseIds()));
            profile.setDiseases(diseases);
        }

        if (dto.getAllergenIds() != null) {
            Set<Allergen> allergens = new HashSet<>(allergenRepository.findAllById(dto.getAllergenIds()));
            profile.setAllergens(allergens);
        }

        calculateNutritionTargets(profile);

        HealthProfile saved = healthProfileRepository.save(profile);
        return toDto(saved);
    }

    private void calculateNutritionTargets(HealthProfile profile) {
        if (profile.getWeight() == null || profile.getHeight() == null || profile.getAge() == null) {
            return;
        }

        double bmr;
        if ("MALE".equalsIgnoreCase(profile.getGender())) {
            bmr = 10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * profile.getAge() + 5;
        } else {
            bmr = 10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * profile.getAge() - 161;
        }

        double multiplier = 1.2;
        if (profile.getActivityLevel() != null) {
            multiplier = switch (profile.getActivityLevel().toUpperCase()) {
                case "SEDENTARY" -> 1.2;
                case "LIGHT" -> 1.375;
                case "MODERATE" -> 1.55;
                case "ACTIVE" -> 1.725;
                case "VERY_ACTIVE" -> 1.9;
                default -> 1.2;
            };
        }

        double dailyCalories = bmr * multiplier;

        if (profile.getDiseases() != null && !profile.getDiseases().isEmpty()) {
            for (Disease disease : profile.getDiseases()) {
                if (disease.getRecommendedCaloriesMultiplier() != null) {
                    dailyCalories *= disease.getRecommendedCaloriesMultiplier();
                }
            }
        }

        profile.setDailyCalorieTarget(Math.round(dailyCalories * 100.0) / 100.0);
        profile.setDailyProteinTarget(Math.round((dailyCalories * 0.3) / 4 * 100.0) / 100.0);
        profile.setDailyFatTarget(Math.round((dailyCalories * 0.3) / 9 * 100.0) / 100.0);
        profile.setDailyCarbTarget(Math.round((dailyCalories * 0.4) / 4 * 100.0) / 100.0);
    }

    private HealthProfileDto toDto(HealthProfile profile) {
        HealthProfileDto dto = new HealthProfileDto();
        dto.setId(profile.getId());
        dto.setWeight(profile.getWeight());
        dto.setHeight(profile.getHeight());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setActivityLevel(profile.getActivityLevel());
        dto.setDailyCalorieTarget(profile.getDailyCalorieTarget());
        dto.setDailyProteinTarget(profile.getDailyProteinTarget());
        dto.setDailyFatTarget(profile.getDailyFatTarget());
        dto.setDailyCarbTarget(profile.getDailyCarbTarget());

        if (profile.getDiseases() != null) {
            dto.setDiseaseIds(profile.getDiseases().stream().map(Disease::getId).collect(Collectors.toSet()));
            dto.setDiseaseNames(profile.getDiseases().stream().map(Disease::getName).collect(Collectors.toSet()));
        } else {
            dto.setDiseaseIds(new HashSet<>());
            dto.setDiseaseNames(new HashSet<>());
        }

        if (profile.getAllergens() != null) {
            dto.setAllergenIds(profile.getAllergens().stream().map(Allergen::getId).collect(Collectors.toSet()));
            dto.setAllergenNames(profile.getAllergens().stream().map(Allergen::getName).collect(Collectors.toSet()));
        } else {
            dto.setAllergenIds(new HashSet<>());
            dto.setAllergenNames(new HashSet<>());
        }

        return dto;
    }
}
