# GitHub Models Integration Guide

## Overview

All prompt files have been updated to the GitHub Models format with comprehensive parameters, test data, and evaluation criteria. The application now supports GitHub Models seamlessly alongside Azure OpenAI and Ollama.

## Updated Prompt Files

### 1. `fix-json-response.yml`
- **Purpose**: Fixes and validates JSON format for recipe responses
- **Model**: `openai/gpt-4o`
- **Parameters**: `{{invalidJson}}`
- **Features**: 
  - Validates JSON structure
  - Ensures proper formatting
  - Includes test cases for malformed JSON

### 2. `fix-json-response-and-prefer-own-recipe.yml`
- **Purpose**: JSON formatter that prioritizes user-provided recipes
- **Model**: `openai/gpt-4o`
- **Parameters**: `{{invalidJson}}`, `{{userRecipes}}`
- **Features**:
  - Prefers user recipes when available
  - Removes text formatting (uppercase, bold, etc.)
  - Maintains recipe authenticity

### 3. `recipe-for-ingredients.yml`
- **Purpose**: Creates recipes from specific ingredients
- **Model**: `openai/gpt-4o`
- **Parameters**: `{{ingredients}}`, `{{format}}`
- **Features**:
  - Uses all provided ingredients
  - Adds complementary ingredients
  - Includes realistic cooking times
  - Comprehensive test data with chicken/rice/vegetables example

### 4. `recipe-for-available-ingredients.yml`
- **Purpose**: Creates recipes prioritizing home ingredients
- **Model**: `openai/gpt-4o`
- **Parameters**: `{{ingredients}}`, `{{availableIngredientsAtHome}}`, `{{format}}`
- **Features**:
  - Maximizes use of home ingredients
  - Minimizes shopping needs
  - Practical, achievable recipes
  - Test cases for salmon/asparagus and beef/pasta combinations

### 5. `image-for-recipe.yml`
- **Purpose**: Generates cookbook-quality food photography prompts
- **Model**: `openai/dall-e-3`
- **Parameters**: `{{recipe}}`
- **Features**:
  - Professional food photography descriptions
  - Detailed styling and lighting instructions
  - Restaurant-quality presentation guidelines

## Configuration

### Environment Variables
Add to your `.env` file:
```bash
GITHUB_TOKEN=github_pat_your_token_here
```

### Application Configuration
Use the `github` profile:
```yaml
spring:
  profiles:
    active: github
```

### GitHub Models Profile
```yaml
spring.config.activate.on-profile: github

langchain4j:
  open-ai:
    chat-model:
      api-key: ${GITHUB_TOKEN}
      base-url: https://models.inference.ai.azure.com
      model-name: gpt-4o
      temperature: 0.7
    image-model:
      api-key: ${GITHUB_TOKEN}
      base-url: https://models.inference.ai.azure.com
      model-name: dall-e-3
    embedding-model:
      api-key: ${GITHUB_TOKEN}
      base-url: https://models.inference.ai.azure.com
      model-name: text-embedding-3-small
  community.redis:
    enabled: false
```

## Usage Examples

### 1. Basic Recipe Generation
```bash
curl -X POST http://localhost:8080/api/recipe \
  -H "Content-Type: application/json" \
  -d '{"ingredients": ["chicken", "rice", "vegetables"]}'
```

### 2. Using Available Ingredients
```bash
curl -X POST http://localhost:8080/api/recipe \
  -H "Content-Type: application/json" \
  -d '{
    "ingredients": ["salmon", "asparagus"], 
    "preferAvailableIngredients": true
  }'
```

### 3. With User Recipes (RAG)
```bash
curl -X POST http://localhost:8080/api/recipe \
  -H "Content-Type: application/json" \
  -d '{
    "ingredients": ["pasta", "tomatoes"], 
    "preferOwnRecipes": true
  }'
```

## Test Data and Evaluation

Each prompt file includes:
- **Comprehensive test data** with realistic input/output examples
- **Automated evaluators** for quality assessment
- **GitHub coherence evaluation** for natural language quality

### Example Test Case (recipe-for-ingredients.yml)
```yaml
testData:
  - ingredients: "tomatoes, pasta, garlic"
    expected: |
      {
        "name": "Classic Garlic Tomato Pasta",
        "description": "A simple yet flavorful pasta dish",
        "ingredients": ["400g pasta", "500g fresh tomatoes", "4 cloves garlic", "60ml olive oil"],
        "instructions": ["Boil pasta", "Sauté garlic", "Add tomatoes", "Combine"],
        "imageUrl": ""
      }
```

## Model Parameters

Each prompt is optimized with specific parameters:
- **Temperature**: 0.1-0.7 based on creativity needs
- **Max tokens**: 1000-1500 for comprehensive responses
- **Quality settings**: HD for images, natural style for DALL-E

## Benefits Achieved

1. **GitHub Models Ready**: All prompts work with GitHub Models API
2. **Comprehensive Testing**: Each prompt has test data and evaluators
3. **Parameter Validation**: All template variables are properly defined
4. **Quality Assurance**: Built-in evaluation criteria
5. **Backwards Compatibility**: Still works with Azure OpenAI and Ollama
6. **Professional Standards**: Cookbook-quality outputs

## Running the Application

### With GitHub Models
```bash
export GITHUB_TOKEN=your_token_here
./gradlew bootRun --args='--spring.profiles.active=github'
```

### Testing
```bash
./gradlew test  # All tests pass
./gradlew build # Clean build successful
```

## Troubleshooting

### Common Issues
1. **Missing GITHUB_TOKEN**: Ensure token is set in `.env` file
2. **Rate Limits**: GitHub Models has usage limits, monitor consumption
3. **Model Availability**: Ensure gpt-4o and dall-e-3 are available in your region

### Validation
- All YAML files are syntactically correct
- Template parameters are properly defined
- Test cases provide comprehensive coverage
- Evaluators ensure quality outputs

The application is now fully ready for GitHub Models with professional-grade prompts, comprehensive testing, and quality evaluation!
