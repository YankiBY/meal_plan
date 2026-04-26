package com.example.mealplan.service;

import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BarcodeService {

    private final IngredientRepository ingredientRepository;

    public Optional<Ingredient> findByBarcode(String barcode) {
        // First check local DB
        Optional<Ingredient> localIngredient = ingredientRepository.findByBarcode(barcode);
        if (localIngredient.isPresent()) {
            return localIngredient;
        }

        // Use Case 15: Placeholder for external API integration (e.g. OpenFoodFacts)
        // return fetchFromExternalApi(barcode);
        return Optional.empty();
    }
}
