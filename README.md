# Recipe Finder Application

A Spring Boot application built with **LangChain4j** that uses AI to help you find recipes based on available ingredients. The application supports both local AI models via Ollama and cloud-based Azure OpenAI.

Find the same demo using [Spring AI](https://github.com/timosalm/spring-ai-recipe-finder) and [Semantic Kernel for Java](https://github.com/timosalm/ai-recipe-finder/tree/main/semantic-kernel).

## Features

- 🤖 AI-powered recipe suggestions using **LangChain4j**
- 📝 Recipe generation based on available ingredients
- 🔍 Vector search using Redis for recipe storage
- 📄 PDF document processing
- 🖼️ Image generation for recipes
- 🌐 Web interface for easy interaction
- 🔧 Multiple AI provider support (Ollama, Azure OpenAI)

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Git

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd spring-recipe-finder
   ```

2. **Start required services**
   ```bash
   docker-compose up -d
   ```

3. **Choose your AI provider** (see detailed instructions below)

## Running with Ollama (Local AI)

### Prerequisites
- Docker running locally
- At least 8GB RAM available for Ollama

### Setup Instructions

1. **Start Ollama and Redis services**
   ```bash
   docker-compose --profile ollama up -d
   ```

2. **Wait for Ollama to download the model** (first time only)
   ```bash
   docker logs -f ollama-runner
   ```
   Wait until you see the model is ready.

3. **Run the application**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=ollama'
   ```

4. **Access the application**
   Open your browser to: http://localhost:8080

### Ollama Configuration
- **Chat Model**: llama3.2
- **Embedding Model**: llama3.2
- **Base URL**: http://127.0.0.1:11434
- **Redis**: localhost:6379

## Running with Azure OpenAI

### Prerequisites
- Azure OpenAI service instance
- Required model deployments in Azure

### Setup Instructions

1. **Create Azure OpenAI deployments**
   
   In your Azure OpenAI service, create these deployments:
   - **gpt-4o**: For chat/text generation
   - **text-embedding-ada-002**: For embeddings
   - **dall-e-3**: For image generation (optional)

2. **Configure environment variables**
   
   Create or update the `.env` file in the project root:
   ```bash
   # Azure OpenAI Configuration
   AZURE_OPENAI_API_KEY=your-actual-api-key-here
   AZURE_OPENAI_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
   ```

   **To get your credentials:**
   - Go to [Azure Portal](https://portal.azure.com)
   - Navigate to your Azure OpenAI resource
   - Go to "Keys and Endpoint" in the left menu
   - Copy KEY 1 as `AZURE_OPENAI_API_KEY`
   - Copy the Endpoint as `AZURE_OPENAI_ENDPOINT`

3. **Start Redis service**
   ```bash
   docker-compose up -d redis
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=azure'
   ```

5. **Access the application**
   Open your browser to: http://localhost:8080

### Azure OpenAI Configuration
- **Chat Model**: gpt-4o
- **Embedding Model**: text-embedding-ada-002
- **Image Model**: dall-e-3
- **Redis**: localhost:6379

## Environment Variables

The application uses a `.env` file to manage sensitive configuration. This file is automatically loaded by the application.

### Example `.env` file:
```bash
# Azure OpenAI Configuration
AZURE_OPENAI_API_KEY=your-actual-api-key-here
AZURE_OPENAI_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
```

**Important**: Never commit your `.env` file to version control. It's already included in `.gitignore`.

## Application Profiles

The application supports multiple profiles:

### `ollama` profile (default)
- Uses local Ollama for AI models
- Requires Docker containers for Ollama and Redis
- No API keys needed
- Best for development and testing

### `azure` profile
- Uses Azure OpenAI services
- Requires Azure OpenAI API key and endpoint
- Requires Redis container
- Best for production use

## Troubleshooting

### Common Issues

1. **Commons-logging conflict warning**
   - This is handled automatically by excluding commons-logging dependencies
   - You can safely ignore any remaining warnings

2. **Redis connection failed**
   ```bash
   docker-compose up -d redis
   ```

3. **Ollama model not found**
   ```bash
   docker-compose --profile ollama up -d
   docker logs ollama-runner
   ```

4. **Azure OpenAI 404 error**
   - Check your endpoint URL format
   - Verify API key is correct
   - Ensure deployments exist in Azure

5. **Azure OpenAI embedding error**
   - Ensure you have `text-embedding-ada-002` deployment
   - Check that the deployment name matches the configuration

### Checking Application Status

- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Redis**: `docker ps` to check if Redis container is running
- **Ollama**: `docker logs ollama` to check Ollama status

## Development

### Building the application
```bash
./gradlew build
```

### Running tests
```bash
./gradlew test
```

### Clean build
```bash
./gradlew clean build
```

## Project Structure

```
spring-recipe-finder/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── RecipeFinderApplication.java
│   │   │   ├── RecipeFinderConfiguration.java
│   │   │   └── recipe/
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-azure.yaml
│   │       ├── application-ollama-compose.yaml
│   │       ├── prompts/
│   │       ├── static/
│   │       └── templates/
│   └── test/
├── build.gradle
├── compose.yaml
├── .env
└── README.md
```

## Technology Stack

- **Spring Boot 3.4.5**
- **Java 21**
- **LangChain4j** - AI integration
- **Redis** - Vector embeddings storage
- **Thymeleaf** - Web templates
- **Docker** - Container services
- **Gradle** - Build tool

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with both Ollama and Azure profiles
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Need Help?

If you encounter any issues:
1. Check the troubleshooting section above
2. Review the application logs
3. Verify your Docker containers are running
4. Check your environment variables
5. Ensure your AI service (Ollama or Azure) is properly configured
