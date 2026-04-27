package com.example.mealplan.controller;

import com.example.mealplan.dto.MealPlanDto;
import com.example.mealplan.dto.MealPlanGenerateRequest;
import com.example.mealplan.entity.MealPlan;
import com.example.mealplan.service.ExportService;
import com.example.mealplan.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {
    private final MealPlanService mealPlanService;
    private final ExportService exportService;

    @GetMapping
    public ResponseEntity<List<MealPlanDto>> getMyPlans(Authentication authentication) {
        return ResponseEntity.ok(mealPlanService.getPlansForUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealPlanDto> getPlanById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(mealPlanService.getPlanById(id, authentication.getName()));
    }

    @PostMapping("/generate")
    public ResponseEntity<MealPlanDto> generatePlan(Authentication authentication,
                                                     @Valid @RequestBody MealPlanGenerateRequest request) {
        return ResponseEntity.ok(mealPlanService.generatePlan(authentication.getName(), request));
    }

    @PutMapping("/{planId}/meals/{mealId}/replace")
    public ResponseEntity<MealPlanDto> replaceMeal(Authentication authentication,
                                                    @PathVariable Long planId,
                                                    @PathVariable Long mealId,
                                                    @RequestParam Long recipeId) {
        return ResponseEntity.ok(mealPlanService.replaceMeal(authentication.getName(), planId, mealId, recipeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id, Authentication authentication) {
        mealPlanService.deletePlan(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/shopping-list")
    public ResponseEntity<Map<String, Double>> getShoppingList(@PathVariable Long id) {
        MealPlan plan = mealPlanService.getById(id);
        return ResponseEntity.ok(exportService.getShoppingList(plan));
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        MealPlan plan = mealPlanService.getById(id);
        byte[] pdfBytes = exportService.exportToPdf(plan);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=meal_plan_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/export/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id) {
        MealPlan plan = mealPlanService.getById(id);
        byte[] excelBytes = exportService.exportToExcel(plan);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=meal_plan_" + id + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
