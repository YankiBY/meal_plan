package com.example.mealplan.controller;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.Disease;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @RequestParam String role) {
        adminService.changeUserRole(id, role);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/moderation/recipes")
    public ResponseEntity<List<RecipeDto>> getRecipesForModeration() {
        return ResponseEntity.ok(adminService.getRecipesForModeration());
    }

    @PostMapping("/moderation/recipes/{id}")
    public ResponseEntity<Void> moderateRecipe(@PathVariable Long id, @RequestParam boolean approve) {
        adminService.moderateRecipe(id, approve);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/diseases")
    public ResponseEntity<Disease> createDisease(@RequestBody Disease disease) {
        return ResponseEntity.ok(adminService.createDisease(disease));
    }

    @PostMapping("/ingredients")
    public ResponseEntity<Ingredient> createIngredient(@RequestBody Ingredient ingredient) {
        return ResponseEntity.ok(adminService.createIngredient(ingredient));
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        return ResponseEntity.ok(adminService.getActivityStats());
    }
}
