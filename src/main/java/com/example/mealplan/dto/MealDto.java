package com.example.mealplan.dto;

import lombok.Data;

@Data
public class MealDto {
    private Long id;
    private String mealType;
    private Long recipeId;
    private String recipeTitle;
    private Double calories;
    private Double proteins;
    private Double fats;
    private Double carbohydrates;
}
