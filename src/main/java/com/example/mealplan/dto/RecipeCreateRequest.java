package com.example.mealplan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class RecipeCreateRequest {
    @NotBlank
    private String title;
    private String description;
    private String instructions;
    private Integer prepTime;
    private Integer cookTime;
    private List<IngredientAmount> ingredients;
    private List<Long> categoryIds;

    @Data
    public static class IngredientAmount {
        private Long ingredientId;
        private Double amount;
    }
}
