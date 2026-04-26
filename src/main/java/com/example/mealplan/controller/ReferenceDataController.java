package com.example.mealplan.controller;

import com.example.mealplan.entity.Allergen;
import com.example.mealplan.entity.Category;
import com.example.mealplan.entity.Disease;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.repository.AllergenRepository;
import com.example.mealplan.repository.CategoryRepository;
import com.example.mealplan.repository.DiseaseRepository;
import com.example.mealplan.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reference")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final DiseaseRepository diseaseRepository;
    private final AllergenRepository allergenRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    @GetMapping("/diseases")
    public ResponseEntity<List<Disease>> getDiseases() {
        return ResponseEntity.ok(diseaseRepository.findAll());
    }

    @GetMapping("/allergens")
    public ResponseEntity<List<Allergen>> getAllergens() {
        return ResponseEntity.ok(allergenRepository.findAll());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<Ingredient>> getIngredients() {
        return ResponseEntity.ok(ingredientRepository.findAll());
    }
}
