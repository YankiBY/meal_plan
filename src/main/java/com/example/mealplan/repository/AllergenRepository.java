package com.example.mealplan.repository;

import com.example.mealplan.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
}
