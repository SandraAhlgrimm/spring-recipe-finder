package com.example;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ChatModel mockChatModel() {
        return mock(ChatModel.class);
    }

    @Bean
    @Primary
    public ImageModel mockImageModel() {
        return mock(ImageModel.class);
    }

    @Bean
    @Primary
    public EmbeddingModel mockEmbeddingModel() {
        return mock(EmbeddingModel.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public EmbeddingStore<TextSegment> mockEmbeddingStore() {
        return mock(EmbeddingStore.class);
    }

    @Bean
    @Primary
    public EmbeddingStoreIngestor mockEmbeddingStoreIngestor() {
        return mock(EmbeddingStoreIngestor.class);
    }
}
