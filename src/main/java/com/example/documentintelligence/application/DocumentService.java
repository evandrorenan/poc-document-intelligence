package com.example.documentintelligence.application;

import com.example.documentintelligence.domain.model.AnalysisStatus;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.DocumentType;
import com.example.documentintelligence.domain.port.DocumentRepositoryPort;
import com.example.documentintelligence.domain.workflow.DocumentProcessingState;
import com.example.documentintelligence.infrastructure.adapter.DocumentAnalyzer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    
    private final DocumentAnalyzer documentAnalyzer;
    private final DocumentRepositoryPort documentRepository;

    public String submitDocument(String base64Document, DocumentType documentType) {
        String protocol = UUID.randomUUID().toString();
        log.info("Received document submission request. Type: {}, Protocol: {}", documentType, protocol);
        
        // Create initial pending analysis
        DocumentAnalysis pendingAnalysis = DocumentAnalysis.builder()
                .protocol(protocol)
                .documentType(documentType)
                .valid(true)
                .base64Document(base64Document)
                .stepResults(new HashMap<>())
                .currentState(DocumentProcessingState.getInitialState())
                .status(AnalysisStatus.PENDING)
                .analysisDate(LocalDateTime.now())
                .build();

        log.debug("Saving initial pending analysis record");
        documentRepository.save(pendingAnalysis);
        
        // Start async processing
        log.info("Starting async document processing for protocol: {}", protocol);
        processDocumentAsync(pendingAnalysis);
        
        log.debug("Returning protocol to client: {}", protocol);
        return protocol;
    }

    @Async("documentAnalysisExecutor")
    protected void processDocumentAsync(DocumentAnalysis documentAnalysis) {
        log.info("Starting async document analysis. Protocol: {}, Type: {}",
                documentAnalysis.getProtocol(),
                documentAnalysis.getDocumentType());

        try {
            log.debug("Calling document analyzer service");
            DocumentAnalysis analysis = documentAnalyzer.analyzeDocument(documentAnalysis);
            
            log.debug("Updating analysis with status complete and datetime");
            analysis.setExtractedData(analysis.getStepResults().get(DocumentProcessingState.getLastState().getQualifierName()));
            analysis.setAnalysisDate(LocalDateTime.now());
            analysis.setStatus(AnalysisStatus.COMPLETED);
            
            log.debug("Saving completed analysis result");
            documentRepository.save(analysis);
            log.info("Document analysis completed successfully. Protocol: {}, Valid: {}",
                    analysis.getProtocol(),
                    analysis.isValid());
            
        } catch (Exception e) {
            log.error("Error processing document. Protocol: {}, Type: {}, Error: {}", 
                    documentAnalysis.getProtocol(), documentAnalysis.getDocumentType(), e.getMessage(), e);
            
            DocumentAnalysis failedAnalysis = DocumentAnalysis.builder()
                    .protocol(documentAnalysis.getProtocol())
                    .documentType(documentAnalysis.getDocumentType())
                    .status(AnalysisStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .analysisDate(LocalDateTime.now())
                    .build();
            
            log.debug("Saving failed analysis result");
            documentRepository.save(failedAnalysis);
        }
    }

    public DocumentAnalysis getAnalysisResult(String protocol) {
        log.debug("Retrieving analysis result for protocol: {}", protocol);
        DocumentAnalysis result = documentRepository.findByProtocol(protocol)
                .orElseThrow(() -> {
                    log.error("Protocol not found: {}", protocol);
                    return new IllegalArgumentException("Protocol not found: " + protocol);
                });
        log.debug("Found analysis result. Status: {}, Type: {}", result.getStatus(), result.getDocumentType());
        return result;
    }
}
