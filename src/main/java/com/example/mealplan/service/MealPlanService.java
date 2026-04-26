package com.example.mealplan.service;

import com.example.mealplan.entity.MealPlan;
import com.example.mealplan.entity.User;
import com.example.mealplan.repository.MealPlanRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;

    public List<MealPlan> getPlansForUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return mealPlanRepository.findByUserId(user.getId());
    }

    public MealPlan generatePlan(String username, LocalDate start, LocalDate end) {
        // Logic to generate plan based on HealthProfile and available moderated recipes
        // This is a placeholder for the actual generation algorithm
        return null;
    }

    public MealPlan getById(Long id) {
        return mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("План питания не найден"));
    }
}
