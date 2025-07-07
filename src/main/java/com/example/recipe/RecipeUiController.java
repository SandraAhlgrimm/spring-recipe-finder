package com.example.recipe;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.capitalize;

@Controller
@RequestMapping("/")
class RecipeUiController {

    private static final Logger log = LoggerFactory.getLogger(RecipeUiController.class);

    private final RecipeService recipeService;
    private final Optional<ImageModel> imageModel;
    private final ChatModel chatModel;
    private final Optional<EmbeddingModel> embeddingModel;

    RecipeUiController(RecipeService recipeService, Optional<ImageModel> imageModel, ChatModel chatModel, Optional<EmbeddingModel> embeddingModel) {
        this.recipeService = recipeService;
        this.imageModel = imageModel;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
	}

    @GetMapping
    String fetchUI(Model model) {
        var aiModelNames = getAiModelNames();
        model.addAttribute("aiModel", String.join(" & ", aiModelNames));
        if (!model.containsAttribute("fetchRecipeData")) {
            model.addAttribute("fetchRecipeData", new FetchRecipeData());
        }
        return "index";
    }

    @PostMapping
    String fetchRecipeUiFor(FetchRecipeData fetchRecipeData, Model model) throws Exception {
        Recipe recipe;
        try {
            recipe = recipeService.fetchRecipeFor(fetchRecipeData.ingredients(), fetchRecipeData.isPreferAvailableIngredients(), fetchRecipeData.isPreferOwnRecipes());
        } catch (Exception e) {
            log.info("Retry RecipeUiController:fetchRecipeFor after exception caused by LLM");
            recipe = recipeService.fetchRecipeFor(fetchRecipeData.ingredients(), fetchRecipeData.isPreferAvailableIngredients(), fetchRecipeData.isPreferOwnRecipes());
        }
        model.addAttribute("recipe", recipe);
        model.addAttribute("fetchRecipeData", fetchRecipeData);
        return fetchUI(model);
    }

    private List<String> getAiModelNames() {
        var modelNames = new ArrayList<String>();
        
        log.info("Getting AI model names...");
        
        // Chat Model
        var chatModelProvider = chatModel.getClass().getSimpleName().replace("ChatModel", "");
        log.info("Chat model class: {}", chatModel.getClass().getName());
        String chatModelName = extractModelName(chatModel);
        log.info("Chat model name extracted: {}", chatModelName);
        if (chatModelName != null) {
            modelNames.add("%s (Chat: %s)".formatted(chatModelProvider, capitalize(chatModelName)));
        } else {
            modelNames.add("%s (Chat)".formatted(chatModelProvider));
        }

        // Embedding Model
        if (embeddingModel.isPresent()) {
            var embeddingModelProvider = embeddingModel.get().getClass().getSimpleName().replace("EmbeddingModel", "");
            log.info("Embedding model class: {}", embeddingModel.get().getClass().getName());
            String embeddingModelName = extractModelName(embeddingModel.get());
            log.info("Embedding model name extracted: {}", embeddingModelName);
            if (embeddingModelName != null) {
                modelNames.add("%s (Embedding: %s)".formatted(embeddingModelProvider, capitalize(embeddingModelName)));
            } else {
                modelNames.add("%s (Embedding)".formatted(embeddingModelProvider));
            }
        }

        // Image Model
        if (imageModel.isPresent()) {
            var imageModelProvider = imageModel.get().getClass().getSimpleName().replace("ImageModel", "");
            log.info("Image model class: {}", imageModel.get().getClass().getName());
            String imageModelName = extractModelName(imageModel.get());
            log.info("Image model name extracted: {}", imageModelName);
            if (imageModelName != null) {
                modelNames.add("%s (Image: %s)".formatted(imageModelProvider, capitalize(imageModelName)));
            } else {
                modelNames.add("%s (Image)".formatted(imageModelProvider));
            }
        }

        log.info("Final model names: {}", modelNames);
        return modelNames;
    }
    
    private String extractModelName(Object model) {
        // Try multiple approaches to get the model name
        String[] possibleFields = {"deploymentName", "modelName", "model", "deploymentId"};
        
        log.debug("Extracting model name from: {}", model.getClass().getName());
        
        // First try the model object itself
        for (String fieldName : possibleFields) {
            try {
                Object fieldValue = FieldUtils.readField(model, fieldName, true);
                if (fieldValue instanceof String name && !name.isEmpty()) {
                    log.debug("Found model name '{}' in field '{}'", name, fieldName);
                    return name;
                }
            } catch (Exception e) {
                log.debug("Failed to read field '{}': {}", fieldName, e.getMessage());
            }
        }
        
        // Then try the default request parameters if it's a chat model
        if (model instanceof ChatModel) {
            try {
                var defaultOptions = ((ChatModel) model).defaultRequestParameters();
                log.debug("Checking default request parameters: {}", defaultOptions.getClass().getName());
                for (String fieldName : possibleFields) {
                    try {
                        Object fieldValue = FieldUtils.readField(defaultOptions, fieldName, true);
                        if (fieldValue instanceof String name && !name.isEmpty()) {
                            log.debug("Found model name '{}' in default options field '{}'", name, fieldName);
                            return name;
                        }
                    } catch (Exception e) {
                        log.debug("Failed to read default options field '{}': {}", fieldName, e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to get default request parameters: {}", e.getMessage());
            }
        }
        
        // Try toString method as last resort
        try {
            String toString = model.toString();
            if (toString.contains("deploymentName=")) {
                String name = toString.substring(toString.indexOf("deploymentName=") + 15);
                name = name.substring(0, name.indexOf(name.contains(",") ? "," : "}"));
                if (!name.isEmpty() && !name.equals("null")) {
                    log.debug("Extracted model name '{}' from toString", name);
                    return name;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract from toString: {}", e.getMessage());
        }
        
        log.debug("No model name found for {}", model.getClass().getName());
        return null;
    }
}
