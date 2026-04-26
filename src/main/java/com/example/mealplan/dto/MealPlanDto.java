package com.example.mealplan.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MealPlanDto {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<MealPlanDayDto> days;
    private Double totalCalories;
    private Double totalProteins;
    private Double totalFats;
    private Double totalCarbs;
}
