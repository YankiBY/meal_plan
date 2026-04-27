package com.example.mealplan.dto;

import lombok.Data;

@Data
public class ActivityStatsDto {
    private long totalUsers;
    private long totalRecipes;
    private long moderatedRecipes;
    private long pendingRecipes;
    private long totalMealPlans;
    private long totalIngredients;
    private long totalDiseases;
    private long totalAllergens;
    private long blockedUsers;
}
