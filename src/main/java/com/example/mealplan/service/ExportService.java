package com.example.mealplan.service;

import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.entity.MealPlan;
import com.example.mealplan.entity.RecipeIngredient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExportService {

    // Use Case 8: Form product list
    public Map<String, Double> getShoppingList(MealPlan plan) {
        Map<String, Double> shoppingList = new HashMap<>();
        
        plan.getDays().forEach(day -> {
            day.getMeals().forEach(meal -> {
                meal.getRecipe().getRecipeIngredients().forEach(ri -> {
                    String ingredientName = ri.getIngredient().getName();
                    shoppingList.put(ingredientName, shoppingList.getOrDefault(ingredientName, 0.0) + ri.getAmount());
                });
            });
        });
        
        return shoppingList;
    }

    // Use Case 10: Export to PDF (Placeholder)
    public byte[] exportToPdf(MealPlan plan) {
        // Logic to generate PDF using iText or similar
        return "PDF content placeholder".getBytes();
    }

    // Use Case 10: Export to Excel (Placeholder)
    public byte[] exportToExcel(MealPlan plan) {
        // Logic to generate Excel using Apache POI
        return "Excel content placeholder".getBytes();
    }
}
