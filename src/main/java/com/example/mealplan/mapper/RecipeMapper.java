package com.example.mealplan.mapper;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.entity.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipeMapper {
    @Mapping(target = "authorName", source = "author.username")
    RecipeDto toDto(Recipe recipe);
}
