package com.example.documentintelligence.application;

import com.example.documentintelligence.domain.model.AnalysisStatus;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentRepositoryPort;
import com.example.documentintelligence.domain.workflow.DocumentProcessingState;
import com.example.documentintelligence.infrastructure.api.dto.DocumentSubmissionRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentProcessingService documentProcessingService;
    private final DocumentRepositoryPort documentRepository;

    public String submitDocument(DocumentSubmissionRequest request) {
        String protocol = UUID.randomUUID().toString();
        log.info("Received document submission request. Type: {}, Protocol: {}", request.getDocumentValidationRule(), protocol);

        // Create initial pending analysis
        DocumentAnalysis pendingAnalysis = DocumentAnalysis.builder()
                .protocol(protocol)
                .documentValidationRule(request.getDocumentValidationRule())
                .valid(true)
                .base64Document(request.getBase64Document())
                .referenceData(request.getReferenceData())
                .stepResults(new HashMap<>())
                .currentState(DocumentProcessingState.getInitialState())
                .status(AnalysisStatus.PENDING)
                .analysisDate(LocalDateTime.now())
                .build();

        log.debug("Saving initial pending analysis record");
        documentRepository.save(pendingAnalysis);
        
        // Start async processing
        log.info("Starting async document processing for protocol: {}", protocol);
        documentProcessingService.processDocumentAsync(pendingAnalysis);
        
        log.debug("Returning protocol to client: {}", protocol);
        return protocol;
    }


    public DocumentAnalysis getAnalysisResult(String protocol) {
        log.debug("Retrieving analysis result for protocol: {}", protocol);
        DocumentAnalysis result = documentRepository.findByProtocol(protocol)
                                                    .orElseThrow(() -> {
                                                        log.error("Protocol not found: {}", protocol);
                                                        return new IllegalArgumentException("Protocol not found: " + protocol);
                                                    });
        log.debug("Found analysis result. Status: {}, Type: {}", result.getStatus(), result.getDocumentValidationRule());
        return result;
    }
}
