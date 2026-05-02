package com.example.mealplan.service;

import com.example.mealplan.dto.RecipeCreateRequest;
import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.entity.*;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.mapper.RecipeMapper;
import com.example.mealplan.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeService recipeService;

    private User testUser;
    private Recipe testRecipe;
    private RecipeDto testRecipeDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded")
                .roles(new HashSet<>())
                .build();

        testRecipe = Recipe.builder()
                .id(1L)
                .title("Test Recipe")
                .description("Test Description")
                .instructions("Test Instructions")
                .author(testUser)
                .isModerated(true)
                .totalCalories(200.0)
                .totalProteins(15.0)
                .totalFats(8.0)
                .totalCarbohydrates(25.0)
                .recipeIngredients(new ArrayList<>())
                .categories(new HashSet<>())
                .build();

        testRecipeDto = new RecipeDto();
        testRecipeDto.setId(1L);
        testRecipeDto.setTitle("Test Recipe");
        testRecipeDto.setModerated(true);
        testRecipeDto.setTotalCalories(200.0);
    }

    @Test
    void getAllModerated_returnsOnlyModerated() {
        Recipe unmoderated = Recipe.builder()
                .id(2L).title("Unmoderated").isModerated(false).build();

        when(recipeRepository.findByIsModeratedTrue()).thenReturn(List.of(testRecipe));
        when(recipeMapper.toDto(testRecipe)).thenReturn(testRecipeDto);

        List<RecipeDto> result = recipeService.getAllModerated();

        assertEquals(1, result.size());
        assertEquals("Test Recipe", result.get(0).getTitle());
        verify(recipeRepository).findByIsModeratedTrue();
    }

    @Test
    void getAll_returnsAllRecipes() {
        Recipe recipe2 = Recipe.builder().id(2L).title("Recipe 2").isModerated(false).build();
        RecipeDto dto2 = new RecipeDto();
        dto2.setId(2L);
        dto2.setTitle("Recipe 2");

        when(recipeRepository.findAll()).thenReturn(List.of(testRecipe, recipe2));
        when(recipeMapper.toDto(testRecipe)).thenReturn(testRecipeDto);
        when(recipeMapper.toDto(recipe2)).thenReturn(dto2);

        List<RecipeDto> result = recipeService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getById_found_returnsDto() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeMapper.toDto(testRecipe)).thenReturn(testRecipeDto);

        RecipeDto result = recipeService.getById(1L);

        assertNotNull(result);
        assertEquals("Test Recipe", result.getTitle());
    }

    @Test
    void getById_notFound_throws() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> recipeService.getById(99L));
    }

    @Test
    void getByAuthor_returnsUserRecipes() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recipeRepository.findByAuthorId(1L)).thenReturn(List.of(testRecipe));
        when(recipeMapper.toDto(testRecipe)).thenReturn(testRecipeDto);

        List<RecipeDto> result = recipeService.getByAuthor("testuser");

        assertEquals(1, result.size());
        assertEquals("Test Recipe", result.get(0).getTitle());
    }

    @Test
    void getByAuthor_unknownUser_throws() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> recipeService.getByAuthor("nobody"));
    }

    @Test
    void create_withIngredients_calculatesNutrition() {
        Ingredient chicken = Ingredient.builder()
                .id(1L).name("Chicken").unit("g")
                .calories(239.0).proteins(27.0).fats(14.0).carbohydrates(0.0)
                .build();

        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("Chicken Dish");
        request.setDescription("Desc");
        request.setInstructions("Cook it");
        RecipeCreateRequest.IngredientAmount ia = new RecipeCreateRequest.IngredientAmount();
        ia.setIngredientId(1L);
        ia.setAmount(200.0);
        request.setIngredients(List.of(ia));

        RecipeDto expectedDto = new RecipeDto();
        expectedDto.setId(1L);
        expectedDto.setTitle("Chicken Dish");
        expectedDto.setTotalCalories(478.0);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(chicken));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(recipeMapper.toDto(any(Recipe.class))).thenReturn(expectedDto);

        RecipeDto result = recipeService.create("testuser", request);

        assertNotNull(result);
        assertEquals("Chicken Dish", result.getTitle());
        verify(recipeRepository).save(argThat(recipe -> {
            assertEquals(478.0, recipe.getTotalCalories(), 0.01);
            assertEquals(54.0, recipe.getTotalProteins(), 0.01);
            assertEquals(28.0, recipe.getTotalFats(), 0.01);
            assertEquals(0.0, recipe.getTotalCarbohydrates(), 0.01);
            assertFalse(recipe.isModerated());
            return true;
        }));
    }

    @Test
    void create_noIngredients_noNutritionCalculation() {
        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("Simple Recipe");

        RecipeDto expectedDto = new RecipeDto();
        expectedDto.setTitle("Simple Recipe");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(recipeMapper.toDto(any(Recipe.class))).thenReturn(expectedDto);

        RecipeDto result = recipeService.create("testuser", request);

        assertNotNull(result);
        verify(recipeRepository).save(argThat(recipe -> {
            assertNull(recipe.getTotalCalories());
            return true;
        }));
    }

    @Test
    void create_withCategories_setsCategories() {
        Category breakfast = Category.builder().id(1L).name("Breakfast").build();

        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("Breakfast Recipe");
        request.setCategoryIds(List.of(1L));

        RecipeDto expectedDto = new RecipeDto();
        expectedDto.setTitle("Breakfast Recipe");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(breakfast));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(recipeMapper.toDto(any(Recipe.class))).thenReturn(expectedDto);

        recipeService.create("testuser", request);

        verify(recipeRepository).save(argThat(recipe -> {
            assertNotNull(recipe.getCategories());
            assertEquals(1, recipe.getCategories().size());
            return true;
        }));
    }

    @Test
    void create_setsModeratedFalse() {
        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("New Recipe");

        RecipeDto expectedDto = new RecipeDto();
        expectedDto.setTitle("New Recipe");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(recipeMapper.toDto(any(Recipe.class))).thenReturn(expectedDto);

        recipeService.create("testuser", request);

        verify(recipeRepository).save(argThat(recipe -> {
            assertFalse(recipe.isModerated());
            return true;
        }));
    }

    @Test
    void delete_byAuthor_succeeds() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        recipeService.delete("testuser", 1L);

        verify(recipeRepository).delete(testRecipe);
    }
}
