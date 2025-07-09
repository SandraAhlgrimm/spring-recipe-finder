package com.example.recipe;

import org.springframework.stereotype.Service;

@Service
public class RecipeAiServices {

	// For AiService API example without annotations
	public interface Standard {
		Recipe find(dev.langchain4j.data.message.UserMessage userMessage);
	}

	// With the Spring Boot Starter chatModel, tools, etc. will be configured automatically.
	// Therefore, switching to explicit wiring mode is required when more control is needed.
	// To support multiple AI providers without relying on annotations like @Profile,
	// RecipeFinderConfiguration registers the auto-configured ChatModel bean under the generic name "chatModel".
	// This breaks LangChain4j's current automatic wiring mechanism, so other AiService beans must also be wired explicitly.
	
	// Note: These still use fromResource for now - will be replaced with programmatic creation
	// Note: These interfaces are now configured programmatically in AiServicesConfiguration
	// instead of using @AiService annotations to support dynamic prompt loading
	public interface WithTools {
		Recipe find(String ingredients);
	}

	public interface WithRag {
		Recipe find(String ingredients);
	}

	public interface WithToolsAndRag {
		Recipe find(String ingredients);
	}
}