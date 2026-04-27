package com.example.mealplan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NutritionExplanationDto {
    private Double bmr;
    private Double activityMultiplier;
    private List<DiseaseMultiplier> diseaseMultipliers;
    private Double dailyCalories;
    private Double dailyProteins;
    private Double dailyFats;
    private Double dailyCarbs;

    @Data
    @AllArgsConstructor
    public static class DiseaseMultiplier {
        private Long diseaseId;
        private String diseaseName;
        private Double multiplier;
    }
}

