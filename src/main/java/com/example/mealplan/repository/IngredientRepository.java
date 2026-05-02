package com.example.mealplan.repository;

import com.example.mealplan.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByBarcode(String barcode);
    List<Ingredient> findByNameContainingIgnoreCase(String name);
}
