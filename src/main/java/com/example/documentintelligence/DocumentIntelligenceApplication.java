package com.example.documentintelligence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Document Intelligence POC API",
        version = "1.0",
        description = "API for document validation using Azure Document Intelligence"
    )
)
public class DocumentIntelligenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentIntelligenceApplication.class, args);
    }
}
