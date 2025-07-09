package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import dev.langchain4j.model.input.PromptTemplate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class PromptParameterTest {

    @Autowired
    private PromptConfiguration promptConfiguration;

    @Test
    void testAllPromptParametersAreResolved() {
        // Test recipe-for-ingredients with format parameter
        String recipePrompt = promptConfiguration.getRecipeForIngredients();
        assertNotNull(recipePrompt, "Recipe for ingredients prompt should be loaded");
        assertTrue(recipePrompt.contains("{{ingredients}}"), "Should contain ingredients parameter");
        assertTrue(recipePrompt.contains("{{format}}"), "Should contain format parameter");
        
        // Test template resolution
        PromptTemplate template = PromptTemplate.from(recipePrompt);
        String resolvedPrompt = template.apply(Map.of(
            "ingredients", "chicken,rice,vegetables", 
            "format", "JSON structure example"
        )).text();
        
        assertFalse(resolvedPrompt.contains("{{ingredients}}"), "Ingredients parameter should be resolved");
        assertFalse(resolvedPrompt.contains("{{format}}"), "Format parameter should be resolved");
        assertTrue(resolvedPrompt.contains("chicken,rice,vegetables"), "Should contain actual ingredients");
        assertTrue(resolvedPrompt.contains("JSON structure example"), "Should contain actual format");
    }

    @Test
    void testRecipeForAvailableIngredientsParameters() {
        String prompt = promptConfiguration.getRecipeForAvailableIngredients();
        assertNotNull(prompt, "Recipe for available ingredients prompt should be loaded");
        assertTrue(prompt.contains("{{ingredients}}"), "Should contain ingredients parameter");
        assertTrue(prompt.contains("{{availableIngredientsAtHome}}"), "Should contain available ingredients parameter");
        
        // Test template resolution
        PromptTemplate template = PromptTemplate.from(prompt);
        String resolvedPrompt = template.apply(Map.of(
            "ingredients", "tomatoes,pasta",
            "availableIngredientsAtHome", "bacon,onions",
            "format", "JSON structure example"
        )).text();
        
        assertFalse(resolvedPrompt.contains("{{ingredients}}"), "Ingredients parameter should be resolved");
        assertFalse(resolvedPrompt.contains("{{availableIngredientsAtHome}}"), "Available ingredients parameter should be resolved");
        assertTrue(resolvedPrompt.contains("tomatoes,pasta"), "Should contain actual ingredients");
        assertTrue(resolvedPrompt.contains("bacon,onions"), "Should contain actual available ingredients");
    }

    @Test
    void testImagePromptParameters() {
        String prompt = promptConfiguration.getImageForRecipe();
        assertNotNull(prompt, "Image prompt should be loaded");
        assertTrue(prompt.contains("{{recipe}}"), "Should contain recipe parameter");
        
        // Test template resolution
        PromptTemplate template = PromptTemplate.from(prompt);
        String resolvedPrompt = template.apply(Map.of("recipe", "Spaghetti Carbonara")).text();
        
        assertFalse(resolvedPrompt.contains("{{recipe}}"), "Recipe parameter should be resolved");
        assertTrue(resolvedPrompt.contains("Spaghetti Carbonara"), "Should contain actual recipe name");
    }

    @Test
    void testFixJsonResponsePrompts() {
        String fixJsonPrompt = promptConfiguration.getFixJsonResponse();
        String fixJsonWithRecipePrompt = promptConfiguration.getFixJsonResponseAndPreferOwnRecipe();
        
        assertNotNull(fixJsonPrompt, "Fix JSON prompt should be loaded");
        assertNotNull(fixJsonWithRecipePrompt, "Fix JSON with recipe preference prompt should be loaded");
        
        // These prompts should contain JSON structure guidance
        assertTrue(fixJsonPrompt.toLowerCase().contains("json"), "Should contain JSON guidance");
        assertTrue(fixJsonWithRecipePrompt.toLowerCase().contains("json"), "Should contain JSON guidance");
        
        // Recipe preference prompt should mention user recipes
        assertTrue(fixJsonWithRecipePrompt.toLowerCase().contains("user"), "Should mention user recipes");
    }

    @Test
    void testPromptContentQuality() {
        // Test that prompts contain comprehensive instructions
        String recipePrompt = promptConfiguration.getRecipeForIngredients();
        assertTrue(recipePrompt.contains("metric system"), "Should specify metric measurements");
        assertTrue(recipePrompt.toLowerCase().contains("creative"), "Should mention creativity");
        
        String availableIngredientsPrompt = promptConfiguration.getRecipeForAvailableIngredients();
        assertTrue(availableIngredientsPrompt.contains("home"), "Should mention home ingredients");
        
        String imagePrompt = promptConfiguration.getImageForRecipe();
        assertTrue(imagePrompt.contains("professional") || imagePrompt.contains("Professional"), "Should specify professional quality");
        assertTrue(imagePrompt.contains("lighting"), "Should mention lighting requirements");
        
        String fixJsonPrompt = promptConfiguration.getFixJsonResponse();
        assertTrue(fixJsonPrompt.contains("imageUrl") || fixJsonPrompt.contains("JSON"), "Should mention JSON structure");
        assertTrue(fixJsonPrompt.contains("empty string") || fixJsonPrompt.contains("empty"), "Should specify empty string handling");
    }
}
