package com.example;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class PromptConfiguration {
    
    private String fixJsonResponse;
    private String fixJsonResponseAndPreferOwnRecipe;
    private String recipeForIngredients;
    private String recipeForAvailableIngredients;
    private String imageForRecipe;
    
    // System messages for prompts that have them
    private String recipeForIngredientsSystemMessage;
    private String recipeForIngredientsUserMessage;
    private String recipeForAvailableIngredientsSystemMessage;
    private String recipeForAvailableIngredientsUserMessage;

    @PostConstruct
    public void loadPrompts() {
        Yaml yaml = new Yaml();
        
        fixJsonResponse = loadPromptFromFile(yaml, "prompts/fix-json-response.yml");
        fixJsonResponseAndPreferOwnRecipe = loadPromptFromFile(yaml, "prompts/fix-json-response-and-prefer-own-recipe.yml");
        recipeForIngredients = loadPromptFromFile(yaml, "prompts/recipe-for-ingredients.yml");
        recipeForAvailableIngredients = loadPromptFromFile(yaml, "prompts/recipe-for-available-ingredients.yml");
        imageForRecipe = loadPromptFromFile(yaml, "prompts/image-for-recipe.yml");
        
        // Load system message for recipe-for-ingredients
        recipeForIngredientsSystemMessage = loadSystemMessageFromFile(yaml, "prompts/recipe-for-ingredients.yml");
        recipeForIngredientsUserMessage = loadUserMessageFromFile(yaml, "prompts/recipe-for-ingredients.yml");
        
        // Load system and user messages for recipe-for-available-ingredients
        recipeForAvailableIngredientsSystemMessage = loadSystemMessageFromFile(yaml, "prompts/recipe-for-available-ingredients.yml");
        recipeForAvailableIngredientsUserMessage = loadUserMessageFromFile(yaml, "prompts/recipe-for-available-ingredients.yml");
        
        // Load system message for recipe-for-available-ingredients
        recipeForAvailableIngredientsSystemMessage = loadSystemMessageFromFile(yaml, "prompts/recipe-for-available-ingredients.yml");
        recipeForAvailableIngredientsUserMessage = loadUserMessageFromFile(yaml, "prompts/recipe-for-available-ingredients.yml");
    }

    @SuppressWarnings("unchecked")
    private String loadPromptFromFile(Yaml yaml, String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            try (InputStream inputStream = resource.getInputStream()) {
                Map<String, Object> data = yaml.load(inputStream);
                
                // Check if it's the new GitHub Models format with messages
                if (data.containsKey("messages")) {
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("messages");
                    // Combine system and user messages
                    StringBuilder combinedPrompt = new StringBuilder();
                    for (Map<String, Object> message : messages) {
                        String role = (String) message.get("role");
                        String content = (String) message.get("content");
                        if ("system".equals(role)) {
                            combinedPrompt.append(content).append("\n\n");
                        } else if ("user".equals(role)) {
                            combinedPrompt.append(content);
                        }
                    }
                    return combinedPrompt.toString().trim();
                } else {
                    // Fallback to old format
                    return (String) data.get("prompt");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt from " + fileName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private String loadSystemMessageFromFile(Yaml yaml, String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            try (InputStream inputStream = resource.getInputStream()) {
                Map<String, Object> data = yaml.load(inputStream);
                
                // Extract only the system message
                if (data.containsKey("messages")) {
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("messages");
                    for (Map<String, Object> message : messages) {
                        String role = (String) message.get("role");
                        if ("system".equals(role)) {
                            return (String) message.get("content");
                        }
                    }
                }
                return ""; // Return empty string if no system message found
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load system message from " + fileName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private String loadUserMessageFromFile(Yaml yaml, String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            try (InputStream inputStream = resource.getInputStream()) {
                Map<String, Object> data = yaml.load(inputStream);
                
                // Extract only the user message
                if (data.containsKey("messages")) {
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("messages");
                    for (Map<String, Object> message : messages) {
                        String role = (String) message.get("role");
                        if ("user".equals(role)) {
                            return (String) message.get("content");
                        }
                    }
                }
                return ""; // Return empty string if no user message found
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load user message from " + fileName, e);
        }
    }

    public String getFixJsonResponse() {
        return fixJsonResponse;
    }

    public String getFixJsonResponseAndPreferOwnRecipe() {
        return fixJsonResponseAndPreferOwnRecipe;
    }

    public String getRecipeForIngredients() {
        return recipeForIngredients;
    }

    public String getRecipeForAvailableIngredients() {
        return recipeForAvailableIngredients;
    }

    public String getImageForRecipe() {
        return imageForRecipe;
    }

    public String getRecipeForIngredientsSystemMessage() {
        return recipeForIngredientsSystemMessage;
    }

    public String getRecipeForIngredientsUserMessage() {
        return recipeForIngredientsUserMessage;
    }

    public String getRecipeForAvailableIngredientsSystemMessage() {
        return recipeForAvailableIngredientsSystemMessage;
    }

    public String getRecipeForAvailableIngredientsUserMessage() {
        return recipeForAvailableIngredientsUserMessage;
    }
}
