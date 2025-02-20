package com.example.documentintelligence.domain.workflow;

import lombok.Getter;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.*;

@Getter
public enum DocumentProcessingState {

    AZURE_DOCUMENT_INTELLIGENCE(AZURE_DOCUMENT_INTELLIGENCE_ANALYZER),
    AZURE_OPENAI(AZURE_OPENAI_ANALYZER),
    VALIDATE_CONTENT(VALIDATE_FIELD_CONTENT_ANALYZER),
    PERFORM_ACTION(IMPORTED_DATA_ACTION_EXECUTOR);
    private final String qualifierName;

    DocumentProcessingState(String qualifierName) {
        this.qualifierName = qualifierName;
    }

    public static DocumentProcessingState getInitialState() {
        return DocumentProcessingState.AZURE_DOCUMENT_INTELLIGENCE;
    }

    public static DocumentProcessingState getLastState()  {
        return DocumentProcessingState.PERFORM_ACTION;
    }

    public DocumentProcessingState nextState() {
        return switch (this) {
            case AZURE_DOCUMENT_INTELLIGENCE -> AZURE_OPENAI;
            case AZURE_OPENAI -> VALIDATE_CONTENT;
            case VALIDATE_CONTENT -> PERFORM_ACTION;
            case PERFORM_ACTION -> null;
        };
    }
}