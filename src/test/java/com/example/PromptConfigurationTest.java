package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class PromptConfigurationTest {

    @Autowired
    private PromptConfiguration promptConfiguration;

    @Test
    void testPromptLoading() {
        // Test that all prompts are loaded correctly
        String fixJsonResponse = promptConfiguration.getFixJsonResponse();
        String fixJsonResponseAndPreferOwnRecipe = promptConfiguration.getFixJsonResponseAndPreferOwnRecipe();
        String recipeForIngredients = promptConfiguration.getRecipeForIngredients();
        String recipeForAvailableIngredients = promptConfiguration.getRecipeForAvailableIngredients();
        String imageForRecipe = promptConfiguration.getImageForRecipe();

        // Debug output
        System.out.println("Fix JSON Response: " + fixJsonResponse);
        System.out.println("Fix JSON Response and Prefer Own Recipe: " + fixJsonResponseAndPreferOwnRecipe);
        System.out.println("Recipe for Ingredients: " + recipeForIngredients);
        System.out.println("Recipe for Available Ingredients: " + recipeForAvailableIngredients);
        System.out.println("Image for Recipe: " + imageForRecipe);

        assertNotNull(fixJsonResponse, "Fix JSON response prompt should be loaded");
        assertNotNull(fixJsonResponseAndPreferOwnRecipe, "Fix JSON response and prefer own recipe prompt should be loaded");
        assertNotNull(recipeForIngredients, "Recipe for ingredients prompt should be loaded");
        assertNotNull(recipeForAvailableIngredients, "Recipe for available ingredients prompt should be loaded");
        assertNotNull(imageForRecipe, "Image for recipe prompt should be loaded");

        // Test that prompts contain expected content (case insensitive)
        assertTrue(fixJsonResponse.toLowerCase().contains("json"), "Fix JSON prompt should contain 'JSON'");
        assertTrue(recipeForIngredients.toLowerCase().contains("recipe"), "Recipe prompt should contain 'recipe'");
        assertTrue(imageForRecipe.toLowerCase().contains("picture"), "Image prompt should contain 'picture'");
    }
}
