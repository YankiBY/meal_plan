package com.example.mealplan.service;

import com.example.mealplan.dto.IngredientSafetyDto;
import com.example.mealplan.entity.HealthProfile;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.entity.User;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.repository.HealthProfileRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredientSafetyService {

    private final BarcodeService barcodeService;
    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;

    public Optional<IngredientSafetyDto> checkByBarcode(String username, String barcode) {
        Optional<Ingredient> ingredientOpt = barcodeService.findByBarcode(barcode);
        return ingredientOpt.map(ingredient -> buildSafety(username, ingredient));
    }

    public Optional<IngredientSafetyDto> checkByName(String username, String name) {
        Optional<Ingredient> ingredientOpt = barcodeService.searchByName(name);
        return ingredientOpt.map(ingredient -> buildSafety(username, ingredient));
    }

    private IngredientSafetyDto buildSafety(String username, Ingredient ingredient) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        HealthProfile profile = healthProfileRepository.findByUserId(user.getId()).orElse(null);

        if (profile == null || profile.getAllergens() == null || profile.getAllergens().isEmpty()) {
            return new IngredientSafetyDto(ingredient, true, List.of(), "Аллергены не указаны — продукт не ограничен по аллергенам");
        }

        String ingredientName = ingredient.getName() == null ? "" : ingredient.getName().toLowerCase();
        List<String> matched = profile.getAllergens().stream()
                .map(a -> a.getName() == null ? "" : a.getName().trim())
                .filter(n -> !n.isBlank())
                .filter(n -> ingredientName.contains(n.toLowerCase()))
                .distinct()
                .toList();

        boolean allowed = matched.isEmpty();
        String note = allowed ? "Не найдено совпадений по аллергенам" : "Найдены совпадения по аллергенам: " + String.join(", ", matched);
        return new IngredientSafetyDto(ingredient, allowed, matched, note);
    }
}

