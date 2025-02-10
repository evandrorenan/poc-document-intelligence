package com.example.documentintelligence.infrastructure.adapter;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.core.util.BinaryData;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.DocumentType;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_DOCUMENT_INTELLIGENCE_ANALYZER;

@Component
@Qualifier(AZURE_DOCUMENT_INTELLIGENCE_ANALYZER)
@Slf4j
public class AzureDocumentIntelligenceAnalyzer implements DocumentAnalyzerPort {

    public static final String MODEL_ID = "prebuilt-layout";
    private final DocumentIntelligenceClient client;

    @Autowired
    public AzureDocumentIntelligenceAnalyzer(DocumentIntelligenceClient client) {
        this.client = client;
    }

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {

        log.info("Starting document analysis for type: {}", currentAnalysis.getDocumentType());

        log.debug("Decoding base64 document");
        byte[] documentBytes = Base64.getDecoder().decode(currentAnalysis.getBase64Document());
        log.debug("Document decoded, size: {} bytes", documentBytes.length);

        log.debug("Creating analyze options with prebuilt-document model");
        AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(BinaryData.fromBytes(documentBytes));

        log.info("Sending document to Azure for analysis");
        var result = client.beginAnalyzeDocument(MODEL_ID, options)
                           .getFinalResult();

        currentAnalysis.getStepResults().put(AZURE_DOCUMENT_INTELLIGENCE_ANALYZER, result.getContent());

        log.info("Document analysis completed successfully");

        return currentAnalysis;
    }
}
