package com.example.mealplan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MealPlanGenerateRequest {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String name;
    private List<Long> excludeRecipeIds;
    private List<Long> preferredCategoryIds;
}
