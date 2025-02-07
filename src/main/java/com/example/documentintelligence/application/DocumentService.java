package com.example.documentintelligence.application;

import com.example.documentintelligence.domain.model.AnalysisStatus;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.DocumentType;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.example.documentintelligence.domain.port.DocumentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    
    private final DocumentAnalyzerPort documentAnalyzer;
    private final DocumentRepositoryPort documentRepository;

    public String submitDocument(String base64Document, DocumentType documentType) {
        String protocol = UUID.randomUUID().toString();
        log.info("Received document submission request. Type: {}, Protocol: {}", documentType, protocol);
        
        // Create initial pending analysis
        DocumentAnalysis pendingAnalysis = DocumentAnalysis.builder()
                .protocol(protocol)
                .documentType(documentType)
                .status(AnalysisStatus.PENDING)
                .analysisDate(LocalDateTime.now())
                .build();
        
        log.debug("Saving initial pending analysis record");
        documentRepository.save(pendingAnalysis);
        
        // Start async processing
        log.info("Starting async document processing for protocol: {}", protocol);
        processDocumentAsync(base64Document, documentType, protocol);
        
        log.debug("Returning protocol to client: {}", protocol);
        return protocol;
    }

    @Async("documentAnalysisExecutor")
    protected void processDocumentAsync(String base64Document, DocumentType documentType, String protocol) {
        log.info("Starting async document analysis. Protocol: {}, Type: {}", protocol, documentType);
        try {
            log.debug("Calling document analyzer service");
            DocumentAnalysis analysis = documentAnalyzer.analyzeDocument(base64Document, documentType);
            
            log.debug("Updating analysis with protocol and status");
            analysis.setProtocol(protocol);
            analysis.setStatus(AnalysisStatus.COMPLETED);
            
            log.debug("Saving completed analysis result");
            documentRepository.save(analysis);
            log.info("Document analysis completed successfully. Protocol: {}, Valid: {}", protocol, analysis.isValid());
            
        } catch (Exception e) {
            log.error("Error processing document. Protocol: {}, Type: {}, Error: {}", 
                    protocol, documentType, e.getMessage(), e);
            
            DocumentAnalysis failedAnalysis = DocumentAnalysis.builder()
                    .protocol(protocol)
                    .documentType(documentType)
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
