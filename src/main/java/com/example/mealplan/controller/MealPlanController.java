package com.example.mealplan.controller;

import com.example.mealplan.entity.MealPlan;
import com.example.mealplan.service.ExportService;
import com.example.mealplan.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {
    private final MealPlanService mealPlanService;
    private final ExportService exportService;

    @GetMapping
    public ResponseEntity<List<MealPlan>> getMyPlans(Authentication authentication) {
        return ResponseEntity.ok(mealPlanService.getPlansForUser(authentication.getName()));
    }

    @PostMapping("/generate")
    public ResponseEntity<MealPlan> generatePlan(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(mealPlanService.generatePlan(authentication.getName(), start, end));
    }

    @GetMapping("/{id}/shopping-list")
    public ResponseEntity<Map<String, Double>> getShoppingList(@PathVariable Long id) {
        MealPlan plan = mealPlanService.getById(id);
        return ResponseEntity.ok(exportService.getShoppingList(plan));
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        MealPlan plan = mealPlanService.getById(id);
        return ResponseEntity.ok(exportService.exportToPdf(plan));
    }
}
