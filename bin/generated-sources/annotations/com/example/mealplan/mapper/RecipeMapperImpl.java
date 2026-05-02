package com.example.mealplan.mapper;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.entity.Recipe;
import com.example.mealplan.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-27T01:14:35+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RecipeMapperImpl implements RecipeMapper {

    @Override
    public RecipeDto toDto(Recipe recipe) {
        if ( recipe == null ) {
            return null;
        }

        RecipeDto recipeDto = new RecipeDto();

        recipeDto.setAuthorName( recipeAuthorUsername( recipe ) );
        recipeDto.setCookTime( recipe.getCookTime() );
        recipeDto.setDescription( recipe.getDescription() );
        recipeDto.setId( recipe.getId() );
        recipeDto.setInstructions( recipe.getInstructions() );
        recipeDto.setPrepTime( recipe.getPrepTime() );
        recipeDto.setTitle( recipe.getTitle() );
        recipeDto.setTotalCalories( recipe.getTotalCalories() );
        recipeDto.setTotalCarbohydrates( recipe.getTotalCarbohydrates() );
        recipeDto.setTotalFats( recipe.getTotalFats() );
        recipeDto.setTotalProteins( recipe.getTotalProteins() );

        return recipeDto;
    }

    private String recipeAuthorUsername(Recipe recipe) {
        if ( recipe == null ) {
            return null;
        }
        User author = recipe.getAuthor();
        if ( author == null ) {
            return null;
        }
        String username = author.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
