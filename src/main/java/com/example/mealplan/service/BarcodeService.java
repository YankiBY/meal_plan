package com.example.mealplan.service;

import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BarcodeService {

    private final IngredientRepository ingredientRepository;

    public Optional<Ingredient> findByBarcode(String barcode) {
        Optional<Ingredient> localIngredient = ingredientRepository.findByBarcode(barcode);
        if (localIngredient.isPresent()) {
            return localIngredient;
        }
        return fetchFromOpenFoodFacts(barcode);
    }

    public Optional<Ingredient> searchByName(String name) {
        return ingredientRepository.findByNameContainingIgnoreCase(name).stream().findFirst();
    }

    @SuppressWarnings("unchecked")
    private Optional<Ingredient> fetchFromOpenFoodFacts(String barcode) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"1".equals(String.valueOf(response.get("status")))) {
                return Optional.empty();
            }

            Map<String, Object> product = (Map<String, Object>) response.get("product");
            if (product == null) return Optional.empty();

            Map<String, Object> nutriments = (Map<String, Object>) product.get("nutriments");
            if (nutriments == null) return Optional.empty();

            String productName = (String) product.get("product_name");
            if (productName == null || productName.isBlank()) {
                productName = "Продукт " + barcode;
            }

            Ingredient ingredient = Ingredient.builder()
                    .name(productName)
                    .barcode(barcode)
                    .unit("г")
                    .calories(toDouble(nutriments.get("energy-kcal_100g")))
                    .proteins(toDouble(nutriments.get("proteins_100g")))
                    .fats(toDouble(nutriments.get("fat_100g")))
                    .carbohydrates(toDouble(nutriments.get("carbohydrates_100g")))
                    .build();

            return Optional.of(ingredientRepository.save(ingredient));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
