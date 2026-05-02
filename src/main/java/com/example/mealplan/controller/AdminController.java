package com.example.mealplan.controller;

import com.example.mealplan.dto.ActivityStatsDto;
import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.dto.UpdateUserRequest;
import com.example.mealplan.dto.UserCreateRequest;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.Allergen;
import com.example.mealplan.entity.Category;
import com.example.mealplan.entity.Disease;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // --- User Management ---

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @PostMapping("/users/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @RequestParam String role) {
        adminService.changeUserRole(id, role);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.resetPassword(id, body.get("password"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // --- Recipe Moderation ---

    @GetMapping("/moderation/recipes")
    public ResponseEntity<List<RecipeDto>> getRecipesForModeration() {
        return ResponseEntity.ok(adminService.getRecipesForModeration());
    }

    @PostMapping("/moderation/recipes/{id}")
    public ResponseEntity<Void> moderateRecipe(@PathVariable Long id, @RequestParam boolean approve) {
        adminService.moderateRecipe(id, approve);
        return ResponseEntity.ok().build();
    }

    // --- Diseases ---

    @GetMapping("/diseases")
    public ResponseEntity<List<Disease>> getAllDiseases() {
        return ResponseEntity.ok(adminService.getAllDiseases());
    }

    @PostMapping("/diseases")
    public ResponseEntity<Disease> createDisease(@RequestBody Disease disease) {
        return ResponseEntity.ok(adminService.createDisease(disease));
    }

    @PutMapping("/diseases/{id}")
    public ResponseEntity<Disease> updateDisease(@PathVariable Long id, @RequestBody Disease disease) {
        return ResponseEntity.ok(adminService.updateDisease(id, disease));
    }

    @DeleteMapping("/diseases/{id}")
    public ResponseEntity<Void> deleteDisease(@PathVariable Long id) {
        adminService.deleteDisease(id);
        return ResponseEntity.ok().build();
    }

    // --- Allergens ---

    @GetMapping("/allergens")
    public ResponseEntity<List<Allergen>> getAllAllergens() {
        return ResponseEntity.ok(adminService.getAllAllergens());
    }

    @PostMapping("/allergens")
    public ResponseEntity<Allergen> createAllergen(@RequestBody Allergen allergen) {
        return ResponseEntity.ok(adminService.createAllergen(allergen));
    }

    @PutMapping("/allergens/{id}")
    public ResponseEntity<Allergen> updateAllergen(@PathVariable Long id, @RequestBody Allergen allergen) {
        return ResponseEntity.ok(adminService.updateAllergen(id, allergen));
    }

    @DeleteMapping("/allergens/{id}")
    public ResponseEntity<Void> deleteAllergen(@PathVariable Long id) {
        adminService.deleteAllergen(id);
        return ResponseEntity.ok().build();
    }

    // --- Ingredients ---

    @GetMapping("/ingredients")
    public ResponseEntity<List<Ingredient>> getAllIngredients() {
        return ResponseEntity.ok(adminService.getAllIngredients());
    }

    @PostMapping("/ingredients")
    public ResponseEntity<Ingredient> createIngredient(@RequestBody Ingredient ingredient) {
        return ResponseEntity.ok(adminService.createIngredient(ingredient));
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<Ingredient> updateIngredient(@PathVariable Long id, @RequestBody Ingredient ingredient) {
        return ResponseEntity.ok(adminService.updateIngredient(id, ingredient));
    }

    @DeleteMapping("/ingredients/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        adminService.deleteIngredient(id);
        return ResponseEntity.ok().build();
    }

    // --- Categories ---

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(adminService.createCategory(category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return ResponseEntity.ok(adminService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    // --- Statistics ---

    @GetMapping("/stats")
    public ResponseEntity<ActivityStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getActivityStats());
    }
}
