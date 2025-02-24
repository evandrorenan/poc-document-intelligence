package com.example.documentintelligence.application;

import com.example.documentintelligence.domain.model.AnalysisStatus;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentRepositoryPort;
import com.example.documentintelligence.domain.workflow.DocumentProcessingState;
import com.example.documentintelligence.infrastructure.adapter.DocumentAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final DocumentAnalyzer documentAnalyzer;
    private final DocumentRepositoryPort documentRepository;

    @Async("documentAnalysisExecutor")
    protected void processDocumentAsync(DocumentAnalysis documentAnalysis) {
        log.info("Starting async document analysis. Protocol: {}, Type: {}",
                documentAnalysis.getProtocol(),
                documentAnalysis.getDocumentValidationRule());

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
                    documentAnalysis.getProtocol(), documentAnalysis.getDocumentValidationRule(), e.getMessage(), e);

            DocumentAnalysis failedAnalysis = DocumentAnalysis.builder()
                                                              .protocol(documentAnalysis.getProtocol())
                                                              .documentValidationRule(documentAnalysis.getDocumentValidationRule())
                                                              .status(AnalysisStatus.FAILED)
                                                              .errorMessage(e.getMessage())
                                                              .analysisDate(LocalDateTime.now())
                                                              .build();

            log.debug("Saving failed analysis result");
            documentRepository.save(failedAnalysis);
        }
    }
}
