package com.example.documentintelligence.infrastructure.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DocumentIntelligenceConfig {

    @Value("${azure.document-intelligence.endpoint}")
    private String endpoint;

    @Value("${azure.document-intelligence.key}")
    private String key;

    @Bean
    public DocumentIntelligenceClient documentIntelligenceClient() {
        log.info("Initializing Azure Document Intelligence client with endpoint: {}", endpoint);
        var client = new DocumentIntelligenceClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
        log.info("Azure Document Intelligence client initialized successfully");
        return client;
    }
}
