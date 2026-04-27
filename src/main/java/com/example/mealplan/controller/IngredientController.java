package com.example.mealplan.controller;

import com.example.mealplan.dto.IngredientSafetyDto;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.service.BarcodeService;
import com.example.mealplan.service.IngredientSafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final BarcodeService barcodeService;
    private final IngredientSafetyService ingredientSafetyService;

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Ingredient> getByBarcode(@PathVariable String barcode) {
        return barcodeService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Ingredient> searchByName(@RequestParam String name) {
        return barcodeService.searchByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/barcode/{barcode}/check")
    public ResponseEntity<IngredientSafetyDto> checkByBarcode(Authentication authentication, @PathVariable String barcode) {
        return ingredientSafetyService.checkByBarcode(authentication.getName(), barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/check")
    public ResponseEntity<IngredientSafetyDto> checkByName(Authentication authentication, @RequestParam String name) {
        return ingredientSafetyService.checkByName(authentication.getName(), name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
