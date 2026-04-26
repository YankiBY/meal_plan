package com.example.mealplan.controller;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.entity.Recipe;
import com.example.mealplan.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeDto>> getAll() {
        return ResponseEntity.ok(recipeService.getAllModerated());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<RecipeDto> create(Authentication authentication, @RequestBody Recipe recipe) {
        // In a real app, this would use a DTO and proper service logic
        return ResponseEntity.ok(recipeService.create(authentication.getName(), recipe));
    }
}
