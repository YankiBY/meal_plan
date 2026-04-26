package com.example.mealplan.service;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.User;
import com.example.mealplan.entity.Recipe;
import com.example.mealplan.entity.Role;
import com.example.mealplan.entity.Disease;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.mapper.RecipeMapper;
import com.example.mealplan.mapper.UserMapper;
import com.example.mealplan.repository.UserRepository;
import com.example.mealplan.repository.RecipeRepository;
import com.example.mealplan.repository.DiseaseRepository;
import com.example.mealplan.repository.AllergenRepository;
import com.example.mealplan.repository.IngredientRepository;
import com.example.mealplan.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final DiseaseRepository diseaseRepository;
    private final AllergenRepository allergenRepository;
    private final IngredientRepository ingredientRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RecipeMapper recipeMapper;

    // Use Case 13: Manage accounts
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    // Use Case 14: Manage user recipes
    public List<RecipeDto> getRecipesForModeration() {
        return recipeRepository.findAll().stream()
                .filter(r -> !r.isModerated())
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void moderateRecipe(Long recipeId, boolean approve) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Рецепт не найден"));
        if (approve) {
            recipe.setModerated(true);
            recipeRepository.save(recipe);
        } else {
            recipeRepository.delete(recipe);
        }
    }

    // Use Case 11: Manage directories
    @Transactional
    public Disease createDisease(Disease disease) {
        return diseaseRepository.save(disease);
    }

    @Transactional
    public Ingredient createIngredient(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    // Use Case 12: Analyze activity (Placeholder)
    public String getActivityStats() {
        long userCount = userRepository.count();
        long recipeCount = recipeRepository.count();
        return String.format("Всего пользователей: %d, Всего рецептов: %d", userCount, recipeCount);
    }
}
