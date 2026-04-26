package com.example.mealplan.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProgressDto {
    private Long id;
    private LocalDate date;
    private Double weight;
    private Double caloriesConsumed;
    private Double proteinsConsumed;
    private Double fatsConsumed;
    private Double carbsConsumed;
    private boolean planComplied;
}
