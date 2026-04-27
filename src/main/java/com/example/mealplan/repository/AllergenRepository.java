package com.example.mealplan.repository;

import com.example.mealplan.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    Optional<Allergen> findByName(String name);
}
