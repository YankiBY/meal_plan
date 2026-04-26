package com.example.mealplan.dto;

import lombok.Data;

@Data
public class RecipeIngredientDto {
    private Long ingredientId;
    private String ingredientName;
    private String unit;
    private Double amount;
    private Double calories;
    private Double proteins;
    private Double fats;
    private Double carbohydrates;
}
