package com.example.mealplan.service;

import com.example.mealplan.dto.*;
import com.example.mealplan.entity.*;
import com.example.mealplan.exception.BadRequestException;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MealRepository mealRepository;

    public List<MealPlanDto> getPlansForUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return mealPlanRepository.findByUserId(user.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MealPlanDto getPlanById(Long id, String username) {
        MealPlan plan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("План питания не найден"));
        if (!plan.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Нет доступа к этому плану");
        }
        return toDto(plan);
    }

    public MealPlan getById(Long id) {
        return mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("План питания не найден"));
    }

    @Transactional
    public MealPlanDto generatePlan(String username, MealPlanGenerateRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала");
        }

        HealthProfile healthProfile = healthProfileRepository.findByUserId(user.getId()).orElse(null);

        List<Recipe> availableRecipes = recipeRepository.findByIsModeratedTrue();
        if (availableRecipes.isEmpty()) {
            throw new BadRequestException("Нет доступных рецептов для генерации плана");
        }

        if (request.getExcludeRecipeIds() != null) {
            availableRecipes = availableRecipes.stream()
                    .filter(r -> !request.getExcludeRecipeIds().contains(r.getId()))
                    .collect(Collectors.toList());
        }

        if (request.getPreferredCategoryIds() != null && !request.getPreferredCategoryIds().isEmpty()) {
            List<Recipe> preferred = availableRecipes.stream()
                    .filter(r -> r.getCategories() != null && r.getCategories().stream()
                            .anyMatch(c -> request.getPreferredCategoryIds().contains(c.getId())))
                    .collect(Collectors.toList());
            if (!preferred.isEmpty()) {
                availableRecipes = preferred;
            }
        }

        if (healthProfile != null && healthProfile.getAllergens() != null && !healthProfile.getAllergens().isEmpty()) {
            Set<String> allergenNames = healthProfile.getAllergens().stream()
                    .map(a -> a.getName().toLowerCase())
                    .collect(Collectors.toSet());
            availableRecipes = availableRecipes.stream()
                    .filter(r -> {
                        if (r.getRecipeIngredients() == null) return true;
                        return r.getRecipeIngredients().stream()
                                .noneMatch(ri -> allergenNames.contains(ri.getIngredient().getName().toLowerCase()));
                    })
                    .collect(Collectors.toList());
        }

        if (availableRecipes.isEmpty()) {
            throw new BadRequestException("Нет подходящих рецептов после применения фильтров");
        }

        String planName = request.getName() != null ? request.getName() :
                "План питания " + request.getStartDate() + " - " + request.getEndDate();

        MealPlan mealPlan = MealPlan.builder()
                .name(planName)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .user(user)
                .days(new ArrayList<>())
                .build();

        Random random = new Random();
        long numDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        for (int i = 0; i < numDays; i++) {
            LocalDate date = request.getStartDate().plusDays(i);
            MealPlanDay day = MealPlanDay.builder()
                    .date(date)
                    .mealPlan(mealPlan)
                    .meals(new ArrayList<>())
                    .build();

            for (Meal.MealType mealType : Meal.MealType.values()) {
                Recipe selectedRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
                Meal meal = Meal.builder()
                        .mealType(mealType)
                        .mealPlanDay(day)
                        .recipe(selectedRecipe)
                        .build();
                day.getMeals().add(meal);
            }

            mealPlan.getDays().add(day);
        }

        MealPlan saved = mealPlanRepository.save(mealPlan);
        return toDto(saved);
    }

    @Transactional
    public MealPlanDto replaceMeal(String username, Long planId, Long mealId, Long newRecipeId) {
        MealPlan plan = mealPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("План питания не найден"));
        if (!plan.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Нет доступа к этому плану");
        }

        Recipe newRecipe = recipeRepository.findById(newRecipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Рецепт не найден"));

        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием пищи не найден"));

        meal.setRecipe(newRecipe);
        mealRepository.save(meal);

        return toDto(mealPlanRepository.findById(planId).orElseThrow());
    }

    @Transactional
    public void deletePlan(String username, Long planId) {
        MealPlan plan = mealPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("План питания не найден"));
        if (!plan.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Нет доступа к этому плану");
        }
        mealPlanRepository.delete(plan);
    }

    private MealPlanDto toDto(MealPlan plan) {
        MealPlanDto dto = new MealPlanDto();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());

        double totalCal = 0, totalProt = 0, totalFat = 0, totalCarb = 0;
        List<MealPlanDayDto> dayDtos = new ArrayList<>();

        if (plan.getDays() != null) {
            for (MealPlanDay day : plan.getDays()) {
                MealPlanDayDto dayDto = new MealPlanDayDto();
                dayDto.setId(day.getId());
                dayDto.setDate(day.getDate());

                double dayCal = 0, dayProt = 0, dayFat = 0, dayCarb = 0;
                List<MealDto> mealDtos = new ArrayList<>();

                if (day.getMeals() != null) {
                    for (Meal meal : day.getMeals()) {
                        MealDto mealDto = new MealDto();
                        mealDto.setId(meal.getId());
                        mealDto.setMealType(meal.getMealType().name());
                        if (meal.getRecipe() != null) {
                            mealDto.setRecipeId(meal.getRecipe().getId());
                            mealDto.setRecipeTitle(meal.getRecipe().getTitle());
                            mealDto.setCalories(meal.getRecipe().getTotalCalories());
                            mealDto.setProteins(meal.getRecipe().getTotalProteins());
                            mealDto.setFats(meal.getRecipe().getTotalFats());
                            mealDto.setCarbohydrates(meal.getRecipe().getTotalCarbohydrates());

                            if (meal.getRecipe().getTotalCalories() != null) dayCal += meal.getRecipe().getTotalCalories();
                            if (meal.getRecipe().getTotalProteins() != null) dayProt += meal.getRecipe().getTotalProteins();
                            if (meal.getRecipe().getTotalFats() != null) dayFat += meal.getRecipe().getTotalFats();
                            if (meal.getRecipe().getTotalCarbohydrates() != null) dayCarb += meal.getRecipe().getTotalCarbohydrates();
                        }
                        mealDtos.add(mealDto);
                    }
                }

                dayDto.setMeals(mealDtos);
                dayDto.setDayCalories(dayCal);
                dayDto.setDayProteins(dayProt);
                dayDto.setDayFats(dayFat);
                dayDto.setDayCarbs(dayCarb);
                dayDtos.add(dayDto);

                totalCal += dayCal;
                totalProt += dayProt;
                totalFat += dayFat;
                totalCarb += dayCarb;
            }
        }

        dto.setDays(dayDtos);
        dto.setTotalCalories(totalCal);
        dto.setTotalProteins(totalProt);
        dto.setTotalFats(totalFat);
        dto.setTotalCarbs(totalCarb);

        return dto;
    }
}
