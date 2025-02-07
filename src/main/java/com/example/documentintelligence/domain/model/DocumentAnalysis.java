package com.example.documentintelligence.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class DocumentAnalysis {
    private String protocol;
    private DocumentType documentType;
    private boolean valid;
    private Map<String, String> extractedData;
    private LocalDateTime analysisDate;
    private AnalysisStatus status;
    private String errorMessage;
}
