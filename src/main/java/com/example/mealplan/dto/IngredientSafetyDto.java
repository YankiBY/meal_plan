package com.example.mealplan.dto;

import com.example.mealplan.entity.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IngredientSafetyDto {
    private Ingredient ingredient;
    private boolean allowed;
    private List<String> matchedAllergens;
    private String note;
}

