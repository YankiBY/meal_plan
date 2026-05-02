package com.example.mealplan.service;

import com.example.mealplan.dto.RecipeCreateRequest;
import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.entity.*;
import com.example.mealplan.exception.BadRequestException;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.mapper.RecipeMapper;
import com.example.mealplan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeMapper recipeMapper;

    @Transactional(readOnly = true)
    public List<RecipeDto> getAllModerated() {
        return recipeRepository.findByIsModeratedTrue().stream()
            .map(recipeMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> getAll() {
        return recipeRepository.findAll().stream()
            .map(recipeMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecipeDto getById(Long id) {
        return recipeRepository.findById(id)
            .map(recipeMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Рецепт не найден"));
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> getByAuthor(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return recipeRepository.findByAuthorId(user.getId()).stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecipeDto create(String username, RecipeCreateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Recipe recipe = Recipe.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .prepTime(request.getPrepTime())
                .cookTime(request.getCookTime())
                .author(user)
                .isModerated(false)
                .recipeIngredients(new ArrayList<>())
                .categories(new HashSet<>())
                .build();

        if (request.getIngredients() != null) {
            double totalCalories = 0, totalProteins = 0, totalFats = 0, totalCarbs = 0;
            for (RecipeCreateRequest.IngredientAmount ia : request.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(ia.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ингредиент не найден: " + ia.getIngredientId()));
                RecipeIngredient ri = RecipeIngredient.builder()
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .amount(ia.getAmount())
                        .build();
                recipe.getRecipeIngredients().add(ri);

                double factor = ia.getAmount() / 100.0;
                if (ingredient.getCalories() != null) totalCalories += ingredient.getCalories() * factor;
                if (ingredient.getProteins() != null) totalProteins += ingredient.getProteins() * factor;
                if (ingredient.getFats() != null) totalFats += ingredient.getFats() * factor;
                if (ingredient.getCarbohydrates() != null) totalCarbs += ingredient.getCarbohydrates() * factor;
            }
            recipe.setTotalCalories(Math.round(totalCalories * 100.0) / 100.0);
            recipe.setTotalProteins(Math.round(totalProteins * 100.0) / 100.0);
            recipe.setTotalFats(Math.round(totalFats * 100.0) / 100.0);
            recipe.setTotalCarbohydrates(Math.round(totalCarbs * 100.0) / 100.0);
        }

        if (request.getCategoryIds() != null) {
            recipe.setCategories(new HashSet<>(categoryRepository.findAllById(request.getCategoryIds())));
        }

        return recipeMapper.toDto(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeDto update(String username, Long id, RecipeCreateRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Рецепт не найден"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && !recipe.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("Вы не можете редактировать этот рецепт");
        }

        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setInstructions(request.getInstructions());
        recipe.setPrepTime(request.getPrepTime());
        recipe.setCookTime(request.getCookTime());

        recipe.getRecipeIngredients().clear();
        if (request.getIngredients() != null) {
            double totalCalories = 0, totalProteins = 0, totalFats = 0, totalCarbs = 0;
            for (RecipeCreateRequest.IngredientAmount ia : request.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(ia.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ингредиент не найден"));
                RecipeIngredient ri = RecipeIngredient.builder()
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .amount(ia.getAmount())
                        .build();
                recipe.getRecipeIngredients().add(ri);

                double factor = ia.getAmount() / 100.0;
                if (ingredient.getCalories() != null) totalCalories += ingredient.getCalories() * factor;
                if (ingredient.getProteins() != null) totalProteins += ingredient.getProteins() * factor;
                if (ingredient.getFats() != null) totalFats += ingredient.getFats() * factor;
                if (ingredient.getCarbohydrates() != null) totalCarbs += ingredient.getCarbohydrates() * factor;
            }
            recipe.setTotalCalories(Math.round(totalCalories * 100.0) / 100.0);
            recipe.setTotalProteins(Math.round(totalProteins * 100.0) / 100.0);
            recipe.setTotalFats(Math.round(totalFats * 100.0) / 100.0);
            recipe.setTotalCarbohydrates(Math.round(totalCarbs * 100.0) / 100.0);
        }

        if (request.getCategoryIds() != null) {
            recipe.setCategories(new HashSet<>(categoryRepository.findAllById(request.getCategoryIds())));
        }

        return recipeMapper.toDto(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(String username, Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Рецепт не найден"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && !recipe.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("Вы не можете удалить этот рецепт");
        }

        recipeRepository.delete(recipe);
    }
}
