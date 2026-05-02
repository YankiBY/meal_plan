package com.example.mealplan.service;

import com.example.mealplan.dto.HealthProfileDto;
import com.example.mealplan.dto.NutritionExplanationDto;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthProfileService {
    private final HealthProfileRepository healthProfileRepository;
    private final UserRepository userRepository;
    private final DiseaseRepository diseaseRepository;
    private final AllergenRepository allergenRepository;

    @Transactional(readOnly = true)
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

    public NutritionExplanationDto getNutritionExplanation(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        HealthProfile profile = healthProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Профиль здоровья не заполнен"));

        if (profile.getWeight() == null || profile.getHeight() == null || profile.getAge() == null) {
            throw new RuntimeException("Недостаточно данных для расчёта (нужны вес/рост/возраст)");
        }

        double bmr = calculateBmr(profile);
        double activityMultiplier = resolveActivityMultiplier(profile.getActivityLevel());

        List<NutritionExplanationDto.DiseaseMultiplier> diseaseMultipliers = List.of();
        double calories = bmr * activityMultiplier;
        if (profile.getDiseases() != null && !profile.getDiseases().isEmpty()) {
            diseaseMultipliers = profile.getDiseases().stream()
                    .map(d -> new NutritionExplanationDto.DiseaseMultiplier(d.getId(), d.getName(), d.getRecommendedCaloriesMultiplier()))
                    .collect(Collectors.toList());
            for (Disease disease : profile.getDiseases()) {
                if (disease.getRecommendedCaloriesMultiplier() != null) {
                    calories *= disease.getRecommendedCaloriesMultiplier();
                }
            }
        }

        double dailyCalories = round2(calories);
        double proteins = round2((dailyCalories * 0.3) / 4);
        double fats = round2((dailyCalories * 0.3) / 9);
        double carbs = round2((dailyCalories * 0.4) / 4);

        return new NutritionExplanationDto(
                round2(bmr),
                activityMultiplier,
                diseaseMultipliers,
                dailyCalories,
                proteins,
                fats,
                carbs
        );
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

    private double calculateBmr(HealthProfile profile) {
        if ("MALE".equalsIgnoreCase(profile.getGender())) {
            return 10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * profile.getAge() + 5;
        }
        return 10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * profile.getAge() - 161;
    }

    private double resolveActivityMultiplier(String activityLevel) {
        if (activityLevel == null) return 1.2;
        return switch (activityLevel.toUpperCase()) {
            case "SEDENTARY" -> 1.2;
            case "LIGHT" -> 1.375;
            case "MODERATE" -> 1.55;
            case "ACTIVE" -> 1.725;
            case "VERY_ACTIVE" -> 1.9;
            default -> 1.2;
        };
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void calculateNutritionTargets(HealthProfile profile) {
        if (profile.getWeight() == null || profile.getHeight() == null || profile.getAge() == null) {
            return;
        }

        double bmr = calculateBmr(profile);
        double multiplier = resolveActivityMultiplier(profile.getActivityLevel());

        double dailyCalories = bmr * multiplier;

        if (profile.getDiseases() != null && !profile.getDiseases().isEmpty()) {
            for (Disease disease : profile.getDiseases()) {
                if (disease.getRecommendedCaloriesMultiplier() != null) {
                    dailyCalories *= disease.getRecommendedCaloriesMultiplier();
                }
            }
        }

        profile.setDailyCalorieTarget(round2(dailyCalories));
        profile.setDailyProteinTarget(round2((dailyCalories * 0.3) / 4));
        profile.setDailyFatTarget(round2((dailyCalories * 0.3) / 9));
        profile.setDailyCarbTarget(round2((dailyCalories * 0.4) / 4));
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
