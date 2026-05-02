package com.example.mealplan.repository;

import com.example.mealplan.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByIsModeratedTrue();
    List<Recipe> findByIsModeratedFalse();
    List<Recipe> findByAuthorId(Long authorId);
}
