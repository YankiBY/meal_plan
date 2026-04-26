package com.example.mealplan.service;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.entity.Recipe;
import com.example.mealplan.entity.User;
import com.example.mealplan.mapper.RecipeMapper;
import com.example.mealplan.repository.RecipeRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;

    public List<RecipeDto> getAllModerated() {
        return recipeRepository.findByIsModeratedTrue().stream()
            .map(recipeMapper::toDto)
            .collect(Collectors.toList());
    }

    public RecipeDto getById(Long id) {
        return recipeRepository.findById(id)
            .map(recipeMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Рецепт не найден"));
    }

    @Transactional
    public RecipeDto create(String username, Recipe recipe) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        recipe.setAuthor(user);
        recipe.setModerated(false); // Use Case 9: requires moderation
        return recipeMapper.toDto(recipeRepository.save(recipe));
    }
}
