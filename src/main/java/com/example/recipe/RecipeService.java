package com.example.recipe;

import com.example.PromptConfiguration;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RecipeService {

    private static final Logger log = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeAiServices.WithTools recipeAiServiceWithTools;
    private final RecipeAiServices.WithRag recipeAiServiceWithRag;
    private final RecipeAiServices.WithToolsAndRag recipeAiServiceWithToolsAndRag;
    private final Optional<ImageModel> imageModel;
    private final EmbeddingStoreIngestor embeddingStoreIngestor;
	private final ChatModel chatModel;
    private final PromptConfiguration promptConfiguration;

    @Value("${app.available-ingredients-in-fridge}")
    private List<String> availableIngredientsInFridge;

    // Constructor injection for autoconfigured AI services
    RecipeService(ChatModel chatModel, @Lazy RecipeAiServices.WithTools recipeAiServiceWithTools,
                  @Lazy RecipeAiServices.WithRag recipeAiServiceWithRag, @Lazy RecipeAiServices.WithToolsAndRag recipeAiServiceWithToolsAndRag,
                  Optional<ImageModel> imageModel, EmbeddingStoreIngestor embeddingStoreIngestor, PromptConfiguration promptConfiguration) {
		this.chatModel = chatModel;
        this.recipeAiServiceWithTools = recipeAiServiceWithTools;
        this.recipeAiServiceWithRag = recipeAiServiceWithRag;
        this.recipeAiServiceWithToolsAndRag = recipeAiServiceWithToolsAndRag;
        this.imageModel = imageModel;
        this.embeddingStoreIngestor = embeddingStoreIngestor;
        this.promptConfiguration = promptConfiguration;
	}

    // ETL pipeline orchestrating the flow from raw data sources to a structured vector store
    void addRecipeDocumentForRag(Resource pdfResource) throws IOException {
        log.info("Add recipe document {} for rag", pdfResource.getFilename());

        // Extract: Parses PDF document
        var documentParser = new ApachePdfBoxDocumentParser();
        var document = documentParser.parse(pdfResource.getInputStream());
        // Transforms(Splits text into chunks based on defined character count) and loads data into vector database
        embeddingStoreIngestor.ingest(document);
    }

    Recipe fetchRecipeFor(List<String> ingredients, boolean preferAvailableIngredients, boolean preferOwnRecipes) throws IOException {
        Recipe recipe;
        var ingredientsAsString = String.join(",", ingredients);
        if (!preferAvailableIngredients && !preferOwnRecipes) {
            recipe = fetchRecipeFor(ingredientsAsString);
        } else if (preferAvailableIngredients && !preferOwnRecipes) {
            // Use prompt for available ingredients
            var availableIngredientsAsString = String.join(",", availableIngredientsInFridge);
            var userPrompt = PromptTemplate.from(promptConfiguration.getRecipeForAvailableIngredientsUserMessage())
                    .apply(Map.of(
                        "ingredients", ingredientsAsString,
                        "availableIngredientsAtHome", availableIngredientsAsString
                    ))
                    .text();
            recipe = recipeAiServiceWithTools.find(userPrompt);
        } else if (!preferAvailableIngredients && preferOwnRecipes) {
            // Use prompt for ingredients with RAG
            var jsonFormat = """
                    {
                      "name": "Recipe Name",
                      "description": "Brief description of the dish",
                      "ingredients": ["500g ingredient1", "2 tbsp ingredient2", "1 cup ingredient3"],
                      "instructions": ["Step 1 instruction", "Step 2 instruction", "Step 3 instruction"],
                      "imageUrl": ""
                    }
                    """;
            var userPrompt = PromptTemplate.from(promptConfiguration.getRecipeForIngredientsUserMessage())
                    .apply(Map.of(
                        "ingredients", ingredientsAsString,
                        "format", jsonFormat
                    ))
                    .text();
            recipe = recipeAiServiceWithRag.find(userPrompt);
        } else {
            // Use prompt for ingredients with tools and RAG
            var jsonFormat = """
                    {
                      "name": "Recipe Name",
                      "description": "Brief description of the dish",
                      "ingredients": ["500g ingredient1", "2 tbsp ingredient2", "1 cup ingredient3"],
                      "instructions": ["Step 1 instruction", "Step 2 instruction", "Step 3 instruction"],
                      "imageUrl": ""
                    }
                    """;
            var userPrompt = PromptTemplate.from(promptConfiguration.getRecipeForIngredientsUserMessage())
                    .apply(Map.of(
                        "ingredients", ingredientsAsString,
                        "format", jsonFormat
                    ))
                    .text();
            recipe = recipeAiServiceWithToolsAndRag.find(userPrompt);
        }

        if (imageModel.isPresent()) {
            log.info("Image generation for recipe '{}' started", recipe.name());
            try {
                // Only low-level API available for image models
                var imagePromptTemplate = PromptTemplate.from(promptConfiguration.getImageForRecipe())
                            .apply(Map.of("recipe", recipe.name()));
                var generatedImage = imageModel.get().generate(imagePromptTemplate.text()).content();
                return new Recipe(recipe, generatedImage.url().toString());
            } catch (Exception e) {
                log.warn("Image generation failed for recipe '{}': {}. Returning recipe without image.", recipe.name(), e.getMessage());
                // Return recipe without image if image generation fails
                return recipe;
            }
        }

        return recipe;
    }

    // AiService API without annotations
    private Recipe fetchRecipeFor(String ingredientsAsString) throws IOException {
        // Get system prompt from YAML configuration
        var systemPrompt = promptConfiguration.getRecipeForIngredientsSystemMessage();
        
        // Get user prompt template from YAML configuration
        var userPromptTemplate = promptConfiguration.getRecipeForIngredientsUserMessage();

        // JSON format schema for structured output
        var jsonFormat = """
                {
                  "name": "Recipe Name",
                  "description": "Brief description of the dish",
                  "ingredients": ["500g ingredient1", "2 tbsp ingredient2", "1 cup ingredient3"],
                  "instructions": ["Step 1 instruction", "Step 2 instruction", "Step 3 instruction"],
                  "imageUrl": ""
                }
                """;

        // Builder for high-abstraction API
        var recipeAiService = AiServices.builder(RecipeAiServices.Standard.class)
                .chatModel(chatModel)
                .systemMessageProvider(chatMemoryId -> systemPrompt)
                .build();

        // Helper class for prompt templating
        var userMessage = PromptTemplate.from(userPromptTemplate)
                .apply(Map.of(
                    "ingredients", ingredientsAsString,
                    "format", jsonFormat
                ))
                .toUserMessage();

        // The result is of type Recipe.class due to structured output
        return recipeAiService.find(userMessage);
    }

    // Defines a tool
    @Tool("Fetches ingredients that are available at home")
    List<String> fetchIngredientsAvailableAtHome() {
        log.info("Fetching ingredients available at home function called by LLM");
        return availableIngredientsInFridge;
    }
}