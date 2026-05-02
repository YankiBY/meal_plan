package com.example.mealplan.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MealPlanDayDto {
    private Long id;
    private LocalDate date;
    private List<MealDto> meals;
    private Double dayCalories;
    private Double dayProteins;
    private Double dayFats;
    private Double dayCarbs;
}
