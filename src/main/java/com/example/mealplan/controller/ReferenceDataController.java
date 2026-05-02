package com.example.mealplan.controller;

import com.example.mealplan.entity.Allergen;
import com.example.mealplan.entity.Category;
import com.example.mealplan.entity.Disease;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.exception.BadRequestException;
import com.example.mealplan.repository.AllergenRepository;
import com.example.mealplan.repository.CategoryRepository;
import com.example.mealplan.repository.DiseaseRepository;
import com.example.mealplan.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/diseases")
    public ResponseEntity<Disease> createDisease(@RequestBody Disease disease) {
        if (disease.getName() == null || disease.getName().isBlank()) {
            throw new BadRequestException("Название заболевания обязательно");
        }
        diseaseRepository.findByName(disease.getName().trim())
                .ifPresent(d -> { throw new BadRequestException("Заболевание с таким названием уже существует"); });
        disease.setId(null);
        if (disease.getRecommendedCaloriesMultiplier() == null) {
            disease.setRecommendedCaloriesMultiplier(1.0);
        }
        return ResponseEntity.ok(diseaseRepository.save(disease));
    }

    @GetMapping("/allergens")
    public ResponseEntity<List<Allergen>> getAllergens() {
        return ResponseEntity.ok(allergenRepository.findAll());
    }

    @PostMapping("/allergens")
    public ResponseEntity<Allergen> createAllergen(@RequestBody Allergen allergen) {
        if (allergen.getName() == null || allergen.getName().isBlank()) {
            throw new BadRequestException("Название аллергена обязательно");
        }
        allergenRepository.findByName(allergen.getName().trim())
                .ifPresent(a -> { throw new BadRequestException("Аллерген с таким названием уже существует"); });
        allergen.setId(null);
        return ResponseEntity.ok(allergenRepository.save(allergen));
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
