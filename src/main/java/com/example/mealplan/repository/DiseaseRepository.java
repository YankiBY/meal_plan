package com.example.mealplan.repository;

import com.example.mealplan.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    Optional<Disease> findByName(String name);
}
