package com.example.mealplan.dto;

import lombok.Data;
import java.util.Set;

@Data
public class HealthProfileDto {
    private Double weight;
    private Double height;
    private Integer age;
    private String gender;
    private String activityLevel;
    private Double dailyCalorieTarget;
    private Double dailyProteinTarget;
    private Double dailyFatTarget;
    private Double dailyCarbTarget;
    private Set<String> diseases;
    private Set<String> allergens;
}
