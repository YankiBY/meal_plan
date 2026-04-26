package com.example.mealplan.repository;

import com.example.mealplan.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private RecipeRepository recipeRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private DiseaseRepository diseaseRepository;
    @Autowired private AllergenRepository allergenRepository;
    @Autowired private HealthProfileRepository healthProfileRepository;
    @Autowired private UserProgressRepository progressRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.save(Role.builder().name("ROLE_USER_" + System.nanoTime()).build());
        savedUser = userRepository.save(User.builder()
                .username("repotest" + System.nanoTime())
                .email("repo" + System.nanoTime() + "@test.com")
                .password("encoded")
                .roles(new HashSet<>(Set.of(role)))
                .build());
    }

    @Test
    void userRepository_findByUsername() {
        Optional<User> found = userRepository.findByUsername(savedUser.getUsername());
        assertTrue(found.isPresent());
        assertEquals(savedUser.getEmail(), found.get().getEmail());
    }

    @Test
    void userRepository_findByEmail() {
        Optional<User> found = userRepository.findByEmail(savedUser.getEmail());
        assertTrue(found.isPresent());
        assertEquals(savedUser.getUsername(), found.get().getUsername());
    }

    @Test
    void userRepository_findByUsername_notFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void recipeRepository_findByIsModeratedTrue() {
        Recipe moderated = recipeRepository.save(Recipe.builder()
                .title("Moderated").author(savedUser).isModerated(true)
                .recipeIngredients(new ArrayList<>()).categories(new HashSet<>()).build());
        Recipe unmoderated = recipeRepository.save(Recipe.builder()
                .title("Unmoderated").author(savedUser).isModerated(false)
                .recipeIngredients(new ArrayList<>()).categories(new HashSet<>()).build());

        List<Recipe> moderatedRecipes = recipeRepository.findByIsModeratedTrue();

        assertTrue(moderatedRecipes.stream().anyMatch(r -> r.getTitle().equals("Moderated")));
        assertFalse(moderatedRecipes.stream().anyMatch(r -> r.getTitle().equals("Unmoderated")));
    }

    @Test
    void recipeRepository_findByAuthorId() {
        recipeRepository.save(Recipe.builder()
                .title("Author Recipe").author(savedUser).isModerated(false)
                .recipeIngredients(new ArrayList<>()).categories(new HashSet<>()).build());

        List<Recipe> userRecipes = recipeRepository.findByAuthorId(savedUser.getId());

        assertFalse(userRecipes.isEmpty());
        assertTrue(userRecipes.stream().allMatch(r -> r.getAuthor().getId().equals(savedUser.getId())));
    }

    @Test
    void ingredientRepository_findByBarcode() {
        ingredientRepository.save(Ingredient.builder()
                .name("Barcode Product " + System.nanoTime())
                .barcode("12345678901234")
                .unit("g").calories(100.0).proteins(5.0).fats(2.0).carbohydrates(20.0)
                .build());

        Optional<Ingredient> found = ingredientRepository.findByBarcode("12345678901234");
        assertTrue(found.isPresent());
        assertEquals(100.0, found.get().getCalories());
    }

    @Test
    void ingredientRepository_findByNameContainingIgnoreCase() {
        ingredientRepository.save(Ingredient.builder()
                .name("Special Ingredient ABC" + System.nanoTime())
                .unit("g").calories(50.0).build());

        List<Ingredient> results = ingredientRepository.findByNameContainingIgnoreCase("special ingredient");
        assertFalse(results.isEmpty());
    }

    @Test
    void healthProfileRepository_findByUserId() {
        HealthProfile profile = healthProfileRepository.save(HealthProfile.builder()
                .user(savedUser).weight(80.0).height(180.0).age(30)
                .gender("MALE").activityLevel("MODERATE")
                .dailyCalorieTarget(2759.0)
                .diseases(new HashSet<>()).allergens(new HashSet<>())
                .build());

        Optional<HealthProfile> found = healthProfileRepository.findByUserId(savedUser.getId());

        assertTrue(found.isPresent());
        assertEquals(80.0, found.get().getWeight());
        assertEquals(2759.0, found.get().getDailyCalorieTarget());
    }

    @Test
    void userProgressRepository_findByUserId() {
        progressRepository.save(UserProgress.builder()
                .user(savedUser).date(LocalDate.now())
                .weight(80.0).caloriesConsumed(2000.0)
                .build());

        List<UserProgress> progress = progressRepository.findByUserId(savedUser.getId());
        assertEquals(1, progress.size());
    }

    @Test
    void userProgressRepository_findByDateBetween() {
        progressRepository.save(UserProgress.builder()
                .user(savedUser).date(LocalDate.of(2025, 1, 15))
                .weight(80.0).build());
        progressRepository.save(UserProgress.builder()
                .user(savedUser).date(LocalDate.of(2025, 2, 15))
                .weight(79.0).build());

        List<UserProgress> jan = progressRepository.findByUserIdAndDateBetween(
                savedUser.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        assertEquals(1, jan.size());
        assertEquals(80.0, jan.get(0).getWeight());
    }

    @Test
    void diseaseRepository_savesAndFinds() {
        Disease disease = diseaseRepository.save(Disease.builder()
                .name("Test Disease " + System.nanoTime())
                .description("Test")
                .recommendedCaloriesMultiplier(0.9)
                .build());

        Optional<Disease> found = diseaseRepository.findById(disease.getId());
        assertTrue(found.isPresent());
        assertEquals(0.9, found.get().getRecommendedCaloriesMultiplier());
    }

    @Test
    void allergenRepository_savesAndFinds() {
        Allergen allergen = allergenRepository.save(Allergen.builder()
                .name("Test Allergen " + System.nanoTime())
                .build());

        Optional<Allergen> found = allergenRepository.findById(allergen.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void healthProfile_withDiseasesAndAllergens() {
        Disease disease = diseaseRepository.save(Disease.builder()
                .name("HP Disease " + System.nanoTime())
                .description("Test disease")
                .build());
        Allergen allergen = allergenRepository.save(Allergen.builder()
                .name("HP Allergen " + System.nanoTime())
                .build());

        HealthProfile profile = healthProfileRepository.save(HealthProfile.builder()
                .user(savedUser)
                .weight(75.0).height(175.0).age(28)
                .gender("FEMALE").activityLevel("LIGHT")
                .diseases(new HashSet<>(Set.of(disease)))
                .allergens(new HashSet<>(Set.of(allergen)))
                .build());

        Optional<HealthProfile> found = healthProfileRepository.findByUserId(savedUser.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getDiseases().size());
        assertEquals(1, found.get().getAllergens().size());
    }
}
