package com.example.mealplan.controller;

import com.example.mealplan.dto.LoginRequest;
import com.example.mealplan.dto.RecipeCreateRequest;
import com.example.mealplan.dto.RegistrationRequest;
import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.repository.IngredientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecipeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IngredientRepository ingredientRepository;

    private String userToken;
    private static int counter = 0;

    @BeforeEach
    void setUp() throws Exception {
        counter++;
        String uniqueId = String.valueOf(counter) + System.currentTimeMillis() % 10000;
        String username = "rcp" + uniqueId;
        String email = "rcp" + uniqueId + "@t.com";

        RegistrationRequest regRequest = new RegistrationRequest();
        regRequest.setUsername(username);
        regRequest.setEmail(email);
        regRequest.setPassword("password123");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        assertNotNull(loginBody.get("token"), "Login response must contain token");
        userToken = loginBody.get("token").asText();
    }

    @Test
    void createRecipe_simple_returns200() throws Exception {
        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("Integration Test Recipe " + System.nanoTime());
        request.setDescription("Created during integration test");
        request.setInstructions("Mix and serve");

        mockMvc.perform(post("/api/recipes")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(startsWith("Integration Test Recipe")))
                .andExpect(jsonPath("$.moderated").value(false));
    }

    @Test
    void createRecipe_withIngredients_calculatesNutrition() throws Exception {
        Ingredient chicken = ingredientRepository.save(
                Ingredient.builder()
                        .name("IntTestChicken" + System.nanoTime())
                        .unit("g")
                        .calories(165.0)
                        .proteins(31.0)
                        .fats(3.6)
                        .carbohydrates(0.0)
                        .build()
        );

        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("Chicken Test Recipe " + System.nanoTime());
        RecipeCreateRequest.IngredientAmount ia = new RecipeCreateRequest.IngredientAmount();
        ia.setIngredientId(chicken.getId());
        ia.setAmount(200.0);
        request.setIngredients(List.of(ia));

        mockMvc.perform(post("/api/recipes")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(330.0))
                .andExpect(jsonPath("$.totalProteins").value(62.0))
                .andExpect(jsonPath("$.totalFats").value(7.2));
    }

    @Test
    void getMyRecipes_returnsOwnRecipes() throws Exception {
        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("My Recipe " + System.nanoTime());
        request.setDescription("Mine");

        mockMvc.perform(post("/api/recipes")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/recipes/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getAllRecipes_returnsArray() throws Exception {
        mockMvc.perform(get("/api/recipes")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createRecipe_noAuth_returns403() throws Exception {
        RecipeCreateRequest request = new RecipeCreateRequest();
        request.setTitle("No Auth Recipe");

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
