package com.example.documentintelligence.infrastructure.api.dto;

import com.example.documentintelligence.domain.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentSubmissionRequest {
    @NotBlank(message = "Document content is required")
    private String base64Document;
    
    @NotNull(message = "Document type is required")
    private DocumentType documentType;
}
