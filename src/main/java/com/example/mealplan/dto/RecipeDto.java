package com.example.mealplan.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecipeDto {
    private Long id;
    private String title;
    private String description;
    private String instructions;
    private Integer prepTime;
    private Integer cookTime;
    private Double totalCalories;
    private Double totalProteins;
    private Double totalFats;
    private Double totalCarbohydrates;
    private String imagePath;
    private boolean moderated;
    private String authorName;
    private Long authorId;
    private List<RecipeIngredientDto> ingredients;
    private List<String> categories;
}
