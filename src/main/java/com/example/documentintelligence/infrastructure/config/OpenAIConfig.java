package com.example.documentintelligence.infrastructure.config;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration for Azure OpenAI service.
 * Provides beans for OpenAI client and related configuration.
 */
@Configuration
public class OpenAIConfig {

    @Value("${azure.openai.endpoint}")
    private String endpoint;

    @Value("${azure.openai.key}")
    private String key;

    /**
     * Creates an OpenAI client configured with Azure credentials.
     *
     * @return Configured OpenAI client
     * @throws IllegalStateException if required configuration is missing
     */
    @Bean
    public OpenAIClient openAIClient() {
        validateConfiguration();
        
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalStateException("Azure OpenAI endpoint must be configured");
        }
        if (!StringUtils.hasText(key)) {
            throw new IllegalStateException("Azure OpenAI key must be configured");
        }
    }
}
