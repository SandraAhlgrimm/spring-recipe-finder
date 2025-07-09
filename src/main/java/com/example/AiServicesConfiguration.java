package com.example;

import com.example.recipe.RecipeAiServices;
import com.example.recipe.RecipeService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class AiServicesConfiguration {

    @Bean
    public RecipeAiServices.WithTools recipeAiServiceWithTools(ChatModel chatModel, RecipeService recipeService, PromptConfiguration promptConfiguration) {
        return AiServices.builder(RecipeAiServices.WithTools.class)
                .chatModel(chatModel)
                .tools(recipeService)
                .systemMessageProvider(chatMemoryId -> promptConfiguration.getFixJsonResponse())
                .build();
    }

    @Bean
    public RecipeAiServices.WithRag recipeAiServiceWithRag(ChatModel chatModel, Optional<ContentRetriever> contentRetriever, PromptConfiguration promptConfiguration) {
        var builder = AiServices.builder(RecipeAiServices.WithRag.class)
                .chatModel(chatModel)
                .systemMessageProvider(chatMemoryId -> promptConfiguration.getFixJsonResponseAndPreferOwnRecipe());
        
        if (contentRetriever.isPresent()) {
            builder.contentRetriever(contentRetriever.get());
        }
        
        return builder.build();
    }

    @Bean
    public RecipeAiServices.WithToolsAndRag recipeAiServiceWithToolsAndRag(ChatModel chatModel, RecipeService recipeService, Optional<ContentRetriever> contentRetriever, PromptConfiguration promptConfiguration) {
        var builder = AiServices.builder(RecipeAiServices.WithToolsAndRag.class)
                .chatModel(chatModel)
                .tools(recipeService)
                .systemMessageProvider(chatMemoryId -> promptConfiguration.getFixJsonResponseAndPreferOwnRecipe());
        
        if (contentRetriever.isPresent()) {
            builder.contentRetriever(contentRetriever.get());
        }
        
        return builder.build();
    }
}
