package com.example.mealplan.service;

import com.example.mealplan.entity.Ingredient;
import com.example.mealplan.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarcodeServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private BarcodeService barcodeService;

    @Test
    void findByBarcode_localMatch_returnsLocal() {
        Ingredient local = Ingredient.builder()
                .id(1L).name("Local Product").barcode("1234567890")
                .calories(100.0).proteins(5.0).fats(3.0).carbohydrates(15.0)
                .build();

        when(ingredientRepository.findByBarcode("1234567890")).thenReturn(Optional.of(local));

        Optional<Ingredient> result = barcodeService.findByBarcode("1234567890");

        assertTrue(result.isPresent());
        assertEquals("Local Product", result.get().getName());
        assertEquals(100.0, result.get().getCalories());
    }

    @Test
    void searchByName_found_returnsFirst() {
        Ingredient ingredient = Ingredient.builder()
                .id(1L).name("Chicken Breast").unit("g")
                .calories(165.0).proteins(31.0).fats(3.6).carbohydrates(0.0)
                .build();

        when(ingredientRepository.findByNameContainingIgnoreCase("chicken")).thenReturn(List.of(ingredient));

        Optional<Ingredient> result = barcodeService.searchByName("chicken");

        assertTrue(result.isPresent());
        assertEquals("Chicken Breast", result.get().getName());
    }

    @Test
    void searchByName_notFound_returnsEmpty() {
        when(ingredientRepository.findByNameContainingIgnoreCase("nonexistent")).thenReturn(List.of());

        Optional<Ingredient> result = barcodeService.searchByName("nonexistent");

        assertFalse(result.isPresent());
    }
}
