package com.example.documentintelligence.domain.model;

import com.example.documentintelligence.domain.workflow.DocumentProcessingState;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the analysis of a document, including its metadata, extracted data, and processing state.
 * This class follows the builder pattern for flexible object creation and maintains the state
 * of document processing through multiple analysis steps.
 */
@Data
@Builder
@JsonDeserialize(builder = DocumentAnalysis.DocumentAnalysisBuilder.class)
public class DocumentAnalysis {

    @JsonPOJOBuilder(withPrefix = "")
    public static class DocumentAnalysisBuilder {}
    
    @NotBlank(message = "Protocol is required")
    private String protocol;
    
    @NotNull(message = "Document type is required")
    private DocumentValidationRule documentValidationRule;
    
    private boolean valid;
    
    @NotBlank(message = "Document content is required")
    private String base64Document;
    
    private String referenceData;

    private Object extractedData;

    @Builder.Default
    private Map<String, Object> stepResults = new HashMap<>();
    
    @Builder.Default
    private LocalDateTime analysisDate = LocalDateTime.now();
    
    @Builder.Default
    private DocumentProcessingState currentState = DocumentProcessingState.getInitialState();
    
    @NotNull(message = "Analysis status is required")
    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.PENDING;
    
    private String errorMessage;

    /**
     * Updates the analysis status and optionally sets an error message.
     * @param newStatus New status to set
     * @param error Optional error message (can be null)
     * @return this instance for method chaining
     */
    public DocumentAnalysis updateStatus(AnalysisStatus newStatus, String error) {
        this.status = newStatus;
        this.errorMessage = error;
        return this;
    }
}
