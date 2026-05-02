package com.example.mealplan.dto;

import lombok.Data;
import java.util.Set;

@Data
public class HealthProfileDto {
    private Long id;
    private Double weight;
    private Double height;
    private Integer age;
    private String gender;
    private String activityLevel;
    private Double dailyCalorieTarget;
    private Double dailyProteinTarget;
    private Double dailyFatTarget;
    private Double dailyCarbTarget;
    private Set<Long> diseaseIds;
    private Set<Long> allergenIds;
    private Set<String> diseaseNames;
    private Set<String> allergenNames;
}
