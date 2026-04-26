package com.example.mealplan.repository;

import com.example.mealplan.entity.MealPlanDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanDayRepository extends JpaRepository<MealPlanDay, Long> {
}
