package com.example.mealplan.mapper;

import com.example.mealplan.dto.RecipeDto;
import com.example.mealplan.entity.Recipe;
import com.example.mealplan.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-27T01:34:45+0300",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.8.jar, environment: Java 17.0.18 (Oracle Corporation)"
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
        recipeDto.setId( recipe.getId() );
        recipeDto.setTitle( recipe.getTitle() );
        recipeDto.setDescription( recipe.getDescription() );
        recipeDto.setInstructions( recipe.getInstructions() );
        recipeDto.setPrepTime( recipe.getPrepTime() );
        recipeDto.setCookTime( recipe.getCookTime() );
        recipeDto.setTotalCalories( recipe.getTotalCalories() );
        recipeDto.setTotalProteins( recipe.getTotalProteins() );
        recipeDto.setTotalFats( recipe.getTotalFats() );
        recipeDto.setTotalCarbohydrates( recipe.getTotalCarbohydrates() );

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
