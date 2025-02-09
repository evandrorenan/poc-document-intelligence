package com.example.documentintelligence.infrastructure.adapter;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.*;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;

@Component
@Qualifier(AZURE_OPENAI_ANALYZER)
@Slf4j
public class AzureOpenAIAnalyzer implements DocumentAnalyzerPort {
    private final OpenAIClient client;
    private final String deploymentOrModelId;
    private final String azureOpenAIPrompt;

    private final ObjectMapper mapper;

    private final String azureOpenAIContext;

    @Autowired
    public AzureOpenAIAnalyzer(OpenAIClient client,
                               String deploymentOrModelId,
                               String azureOpenAIContext,
                               String azureOpenAIPrompt) {
        this.client = client;
        this.deploymentOrModelId = deploymentOrModelId;
        this.azureOpenAIContext = azureOpenAIContext;
        this.azureOpenAIPrompt = azureOpenAIPrompt;
        this.mapper = new ObjectMapper();
    }

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {
        try {

            String fieldList = String.join("\n", currentAnalysis.getDocumentType().getFields());
            String content = String.join("\n" + currentAnalysis.getStepResults().values());

            String formattedPrompt = String.format(azureOpenAIPrompt, fieldList, content);

            List<ChatRequestMessage> messages = new ArrayList<>();
            messages.add(new ChatRequestSystemMessage(azureOpenAIContext));
            messages.add(new ChatRequestUserMessage(formattedPrompt));

            ChatCompletions completions = client.getChatCompletions(
                deploymentOrModelId,
                new ChatCompletionsOptions(messages)
            );

            String response = completions.getChoices().get(0).getMessage().getContent();
            if (!isValidJson(response)) return currentAnalysis;

            currentAnalysis.getStepResults().put(AZURE_OPENAI_ANALYZER, response);

            return currentAnalysis;

        } catch (Exception e) {
            log.error("Error processing document with OpenAI: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isValidJson(String response) {
        try {
            JsonNode root = mapper.readTree(response);
            if (root == null || root.isNull()) {
                log.error("AzureAI response is empty or null.");
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("AzureAI doesn't return a valid json as expected.");
            return false;
        }
        return true;
    }
}
