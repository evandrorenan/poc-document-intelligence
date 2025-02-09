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

    @Value("${azure.openai.deployment-id}")
    private String deploymentId;

    @Value("${azure.openai.context:}")
    private String context;

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

    /**
     * Provides the deployment ID for the OpenAI model.
     *
     * @return Deployment ID string
     */
    @Bean
    public String openAIDeploymentId() {
        return deploymentId;
    }

    /**
     * Provides the system context/prompt for OpenAI.
     *
     * @return System context string or empty if not configured
     */
    @Bean
    public String openAIContext() {
        return StringUtils.hasText(context) ? context : "";
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalStateException("Azure OpenAI endpoint must be configured");
        }
        if (!StringUtils.hasText(key)) {
            throw new IllegalStateException("Azure OpenAI key must be configured");
        }
        if (!StringUtils.hasText(deploymentId)) {
            throw new IllegalStateException("Azure OpenAI deployment ID must be configured");
        }
    }
}
