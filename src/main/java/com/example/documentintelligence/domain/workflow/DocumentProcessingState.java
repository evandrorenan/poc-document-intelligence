package com.example.documentintelligence.domain.workflow;

import lombok.Getter;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_DOCUMENT_INTELLIGENCE_ANALYZER;

@Getter
public enum DocumentProcessingState {

    AZURE_DOCUMENT_INTELLIGENCE(AZURE_DOCUMENT_INTELLIGENCE_ANALYZER),
    AZURE_OPENAI(AZURE_OPENAI_ANALYZER);
    private final String qualifierName;

    DocumentProcessingState(String qualifierName) {
        this.qualifierName = qualifierName;
    }

    public static DocumentProcessingState getInitialState() {
        return DocumentProcessingState.AZURE_DOCUMENT_INTELLIGENCE;
    }

    public static DocumentProcessingState getLastState()  {
        return DocumentProcessingState.AZURE_OPENAI;
    }

    public DocumentProcessingState nextState() {
        return switch (this) {
            case AZURE_DOCUMENT_INTELLIGENCE -> AZURE_OPENAI;
            case AZURE_OPENAI -> null;
        };
    }
}