package com.openrank.openrank.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangchainConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${langchain4j.chat-model.api-key:}") String apiKey,
            @Value("${langchain4j.chat-model.base-url:}") String baseUrl,
            @Value("${langchain4j.chat-model.model-name:gpt-3.5-turbo}") String modelName
    ) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl == null || baseUrl.isBlank() ? null : baseUrl)
                .modelName(modelName)
                .build();
    }
}
