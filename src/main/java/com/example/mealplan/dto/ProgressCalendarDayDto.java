package com.example.mealplan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ProgressCalendarDayDto {
    private LocalDate date;
    private boolean hasEntry;
    private boolean planComplied;
    private Double weight;
}

