package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.AnalysisStatus;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.example.documentintelligence.domain.workflow.DocumentProcessingState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Coordinates the document analysis workflow by managing state transitions and delegating to appropriate analyzers.
 * Uses the State pattern to process documents through multiple analysis steps, with each state being independent
 * and unaware of other states.
 */
@Component
@Slf4j
public class DocumentAnalyzer {

    private final ApplicationContext context;

    public DocumentAnalyzer(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Processes a document through all analysis states until completion or failure.
     * Each state performs its analysis and updates the document analysis object.
     *
     * @param documentAnalysis The document analysis to process
     * @return Updated document analysis with results from all processing steps
     */
    public DocumentAnalysis analyzeDocument(DocumentAnalysis documentAnalysis) {
        try {
            log.info("Starting document analysis for protocol: {}, type: {}", 
                    documentAnalysis.getProtocol(), documentAnalysis.getDocumentType());
            
            DocumentAnalysis analysis = documentAnalysis;
            while (analysis.getCurrentState() != null && analysis.getStatus() != AnalysisStatus.FAILED) {
                analysis = executeAnalysisStep(analysis);
            }
            
            log.info("Completed document analysis for protocol: {}, status: {}", 
                    analysis.getProtocol(), analysis.getStatus());
            return analysis;
            
        } catch (Exception e) {
            log.error("Error during document analysis for protocol: {}", documentAnalysis.getProtocol(), e);
            return documentAnalysis.updateStatus(AnalysisStatus.FAILED,
                    "Unexpected error during analysis: " + e.getMessage());
        }
    }

    /**
     * Executes a single analysis step using the current state's analyzer.
     *
     * @param analysis Current document analysis
     * @return Updated document analysis with results from the current step
     */
    private DocumentAnalysis executeAnalysisStep(DocumentAnalysis analysis) {
        try {
            DocumentAnalyzerPort analyzer = getAnalyzer(analysis.getCurrentState());
            DocumentAnalysis updatedAnalysis = analyzer.analyzeDocument(analysis);
            
            if (updatedAnalysis.getStatus() != AnalysisStatus.FAILED) {
                updatedAnalysis.setCurrentState(getNextState(updatedAnalysis.getCurrentState()));
            }
            
            return updatedAnalysis;
            
        } catch (Exception e) {
            log.error("Error in analysis step: {}", analysis.getCurrentState(), e);
            return analysis.updateStatus(AnalysisStatus.FAILED,
                    "Error in " + analysis.getCurrentState() + ": " + e.getMessage());
        }
    }

    /**
     * Determines the next state in the analysis workflow.
     *
     * @param currentState Current processing state
     * @return Next state or null if processing is complete
     */
    private DocumentProcessingState getNextState(DocumentProcessingState currentState) {
        return currentState == null ? null : currentState.nextState();
    }

    /**
     * Retrieves the appropriate analyzer for the current state.
     *
     * @param state Current processing state
     * @return Analyzer implementation for the current state
     * @throws IllegalStateException if state is null or analyzer not found
     */
    private DocumentAnalyzerPort getAnalyzer(DocumentProcessingState state) {
        if (state == null) {
            throw new IllegalStateException("Analysis state not set");
        }
        
        try {
            return context.getBean(state.getQualifierName(), DocumentAnalyzerPort.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("No analyzer found for state: " + state, e);
        }
    }
}