package com.example.documentintelligence.domain.model;

import com.example.documentintelligence.domain.workflow.DocumentProcessingState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the analysis of a document, including its metadata, extracted data, and processing state.
 * This class follows the builder pattern for flexible object creation and maintains the state
 * of document processing through multiple analysis steps.
 */
@Data
@Builder
public class DocumentAnalysis {
    
    @NotBlank(message = "Protocol is required")
    private String protocol;
    
    @NotNull(message = "Document type is required")
    private DocumentType documentType;
    
    private boolean valid;
    
    @NotBlank(message = "Document content is required")
    private String base64Document;
    
    @Builder.Default
    private Map<String, String> extractedData = new HashMap<>();
    
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
     * Adds extracted data to the analysis results.
     * @param newData Map of field names to extracted values
     * @return this instance for method chaining
     */
    public DocumentAnalysis addExtractedData(Map<String, String> newData) {
        if (newData != null) {
            this.extractedData.putAll(newData);
        }
        return this;
    }

    /**
     * Records the result of a processing step.
     * @param stepName Name of the processing step
     * @param result Result object from the step
     * @return this instance for method chaining
     */
    public DocumentAnalysis addStepResult(String stepName, Object result) {
        if (stepName != null) {
            this.stepResults.put(stepName, result);
        }
        return this;
    }

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
