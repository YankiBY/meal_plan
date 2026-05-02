package com.example.mealplan.mapper;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.dto.RecipeIngredientDto;
import com.example.mealplan.entity.Category;
import com.example.mealplan.entity.Recipe;
import com.example.mealplan.entity.RecipeIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    @Mapping(target = "authorName", source = "author.username")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "ingredients", expression = "java(mapIngredients(recipe.getRecipeIngredients()))")
    @Mapping(target = "categories", expression = "java(mapCategories(recipe.getCategories()))")
    @Mapping(target = "moderated", source = "moderated")
    @Mapping(target = "imagePath", source = "imagePath")
    RecipeDto toDto(Recipe recipe);

    default List<RecipeIngredientDto> mapIngredients(List<RecipeIngredient> ingredients) {
        if (ingredients == null) return null;
        return ingredients.stream().map(ri -> {
            RecipeIngredientDto dto = new RecipeIngredientDto();
            dto.setIngredientId(ri.getIngredient().getId());
            dto.setIngredientName(ri.getIngredient().getName());
            dto.setUnit(ri.getIngredient().getUnit());
            dto.setAmount(ri.getAmount());
            dto.setCalories(ri.getIngredient().getCalories());
            dto.setProteins(ri.getIngredient().getProteins());
            dto.setFats(ri.getIngredient().getFats());
            dto.setCarbohydrates(ri.getIngredient().getCarbohydrates());
            return dto;
        }).collect(Collectors.toList());
    }

    default List<String> mapCategories(Set<Category> categories) {
        if (categories == null) return null;
        return categories.stream().map(Category::getName).collect(Collectors.toList());
    }
}
