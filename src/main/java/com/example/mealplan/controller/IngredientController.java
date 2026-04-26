package com.example.mealplan.controller;

import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.service.BarcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final BarcodeService barcodeService;

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Ingredient> getByBarcode(@PathVariable String barcode) {
        return barcodeService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
