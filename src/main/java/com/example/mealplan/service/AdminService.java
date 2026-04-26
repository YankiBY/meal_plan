package com.example.mealplan.service;

import com.example.mealplan.dto.ActivityStatsDto;
import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.*;
import com.example.mealplan.exception.BadRequestException;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.mapper.RecipeMapper;
import com.example.mealplan.mapper.UserMapper;
import com.example.mealplan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final DiseaseRepository diseaseRepository;
    private final AllergenRepository allergenRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final MealPlanRepository mealPlanRepository;
    private final UserMapper userMapper;
    private final RecipeMapper recipeMapper;
    private final PasswordEncoder passwordEncoder;

    // --- User Management (UC13) ---

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Роль не найдена: " + roleName));
        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        user.setBlocked(false);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }

    // --- Recipe Moderation (UC14) ---

    public List<RecipeDto> getRecipesForModeration() {
        return recipeRepository.findAll().stream()
                .filter(r -> !r.isModerated())
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void moderateRecipe(Long recipeId, boolean approve) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Рецепт не найден"));
        if (approve) {
            recipe.setModerated(true);
            recipeRepository.save(recipe);
        } else {
            recipeRepository.delete(recipe);
        }
    }

    // --- Directory Management (UC11) ---

    // Diseases
    @Transactional
    public Disease createDisease(Disease disease) {
        return diseaseRepository.save(disease);
    }

    @Transactional
    public Disease updateDisease(Long id, Disease disease) {
        Disease existing = diseaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заболевание не найдено"));
        existing.setName(disease.getName());
        existing.setDescription(disease.getDescription());
        existing.setRecommendedCaloriesMultiplier(disease.getRecommendedCaloriesMultiplier());
        return diseaseRepository.save(existing);
    }

    @Transactional
    public void deleteDisease(Long id) {
        if (!diseaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Заболевание не найдено");
        }
        diseaseRepository.deleteById(id);
    }

    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll();
    }

    // Allergens
    @Transactional
    public Allergen createAllergen(Allergen allergen) {
        return allergenRepository.save(allergen);
    }

    @Transactional
    public Allergen updateAllergen(Long id, Allergen allergen) {
        Allergen existing = allergenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Аллерген не найден"));
        existing.setName(allergen.getName());
        return allergenRepository.save(existing);
    }

    @Transactional
    public void deleteAllergen(Long id) {
        if (!allergenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Аллерген не найден");
        }
        allergenRepository.deleteById(id);
    }

    public List<Allergen> getAllAllergens() {
        return allergenRepository.findAll();
    }

    // Ingredients
    @Transactional
    public Ingredient createIngredient(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public Ingredient updateIngredient(Long id, Ingredient ingredient) {
        Ingredient existing = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ингредиент не найден"));
        existing.setName(ingredient.getName());
        existing.setUnit(ingredient.getUnit());
        existing.setCalories(ingredient.getCalories());
        existing.setProteins(ingredient.getProteins());
        existing.setFats(ingredient.getFats());
        existing.setCarbohydrates(ingredient.getCarbohydrates());
        existing.setBarcode(ingredient.getBarcode());
        return ingredientRepository.save(existing);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ингредиент не найден");
        }
        ingredientRepository.deleteById(id);
    }

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    // Categories
    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));
        existing.setName(category.getName());
        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категория не найдена");
        }
        categoryRepository.deleteById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // --- Activity Stats (UC12) ---

    public ActivityStatsDto getActivityStats() {
        ActivityStatsDto stats = new ActivityStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalRecipes(recipeRepository.count());
        stats.setModeratedRecipes(recipeRepository.findByIsModeratedTrue().size());
        stats.setPendingRecipes(stats.getTotalRecipes() - stats.getModeratedRecipes());
        stats.setTotalMealPlans(mealPlanRepository.count());
        stats.setTotalIngredients(ingredientRepository.count());
        stats.setTotalDiseases(diseaseRepository.count());
        stats.setTotalAllergens(allergenRepository.count());
        stats.setBlockedUsers(userRepository.findAll().stream().filter(User::isBlocked).count());
        return stats;
    }
}
