package com.example.documentintelligence.domain.workflow;

public interface AnalyzerQualifiers {
    String AZURE_DOCUMENT_INTELLIGENCE_ANALYZER = "azureDocumentIntelligenceAnalyzer";
    String AZURE_OPENAI_ANALYZER = "azureOpenAIAnalyzer";
    String VALIDATE_FIELD_CONTENT_ANALYZER = "documentDataConsistencyChecker";
    String IMPORTED_DATA_ACTION_EXECUTOR  = "importedDataActionExecutor";
}