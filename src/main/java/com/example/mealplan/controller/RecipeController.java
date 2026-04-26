package com.example.mealplan.controller;

import com.example.mealplan.dto.RecipeCreateRequest;
import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.service.RecipeService;
import jakarta.validation.Valid;
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

    @GetMapping("/all")
    public ResponseEntity<List<RecipeDto>> getAllIncludingUnmoderated() {
        return ResponseEntity.ok(recipeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RecipeDto>> getMyRecipes(Authentication authentication) {
        return ResponseEntity.ok(recipeService.getByAuthor(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<RecipeDto> create(Authentication authentication,
                                            @Valid @RequestBody RecipeCreateRequest request) {
        return ResponseEntity.ok(recipeService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> update(Authentication authentication,
                                            @PathVariable Long id,
                                            @Valid @RequestBody RecipeCreateRequest request) {
        return ResponseEntity.ok(recipeService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        recipeService.delete(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}
