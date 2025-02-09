package com.example.documentintelligence.infrastructure.adapter;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.DocumentType;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class AzureDocumentAnalyzer implements DocumentAnalyzerPort {

    private static final Logger log = LoggerFactory.getLogger(AzureDocumentAnalyzer.class);
    public static final String MODEL_ID = "prebuilt-layout";
    private final DocumentIntelligenceClient client;

    public AzureDocumentAnalyzer(
            @Value("${azure.document-intelligence.endpoint}") String endpoint,
            @Value("${azure.document-intelligence.key}") String key) {
        log.info("Initializing Azure Document Intelligence client with endpoint: {}", endpoint);
        this.client = new DocumentIntelligenceClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
        log.info("Azure Document Intelligence client initialized successfully");
    }

    @Override
    public DocumentAnalysis analyzeDocument(String base64Document, DocumentType documentType) {
        log.info("Starting document analysis for type: {}", documentType);
        try {
            log.debug("Decoding base64 document");
            byte[] documentBytes = Base64.getDecoder().decode(base64Document);
            log.debug("Document decoded, size: {} bytes", documentBytes.length);

            log.debug("Creating analyze options with prebuilt-document model");
            AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(BinaryData.fromBytes(documentBytes));

            log.info("Sending document to Azure for analysis");
            var result = client.beginAnalyzeDocument(MODEL_ID, options)
                    .getFinalResult();

            log.info("Document analysis completed successfully");

            log.debug("Extracting key-value pairs from analysis result");
            Map<String, String> extractedData = new HashMap<>();
            result.getKeyValuePairs().forEach(kvp -> {
                if (kvp.getKey() != null && kvp.getValue() != null) {
                    String key = kvp.getKey().getContent();
                    String value = kvp.getValue().getContent();
                    log.trace("Extracted key-value pair: {} = {}", key, value);
                    extractedData.put(key, value);
                }
            });
            log.debug("Extracted {} key-value pairs", extractedData.size());

            log.debug("Validating document data");
            boolean isValid = validateDocument(extractedData, documentType);
            log.info("Document validation result: {}", isValid);

            DocumentAnalysis analysis = DocumentAnalysis.builder()
                    .documentType(documentType)
                    .valid(isValid)
                    .extractedData(extractedData)
                    .analysisDate(LocalDateTime.now())
                    .build();
            log.info("Document analysis completed. Valid: {}, Extracted fields: {}", isValid, extractedData.size());
            return analysis;

        } catch (Exception e) {
            log.error("Error analyzing document of type {}: {}", documentType, e.getMessage(), e);
            throw e;
        }
    }

    private boolean validateDocument(Map<String, String> extractedData, DocumentType documentType) {
        log.debug("Validating document of type: {}", documentType);
        boolean isValid = switch (documentType) {
            case RG -> validateRG(extractedData);
            case CPF -> validateCPF(extractedData);
            case COMPROVANTE_RESIDENCIA -> validateComprovanteResidencia(extractedData);
            case REGISTRO_MATRICULA, APOLICE_SEGURO -> validateGenericDocument(extractedData);
        };
        log.debug("Document validation result for type {}: {}", documentType, isValid);
        return isValid;
    }

    private boolean validateRG(Map<String, String> data) {
        log.debug("Validating RG document. Required fields: [Número RG, Nome, Data de Nascimento]");
        return data.containsKey("Número RG") && data.containsKey("Nome") && data.containsKey("Data de Nascimento");
    }

    private boolean validateCPF(Map<String, String> data) {
        log.debug("Validating CPF document. Required fields: [Número CPF, Nome]");
        return data.containsKey("Número CPF") && data.containsKey("Nome");
    }

    private boolean validateComprovanteResidencia(Map<String, String> data) {
        log.debug("Validating Comprovante de Residência. Required fields: [Endereço, CEP, Data]");
        return data.containsKey("Endereço") && data.containsKey("CEP") && data.containsKey("Data");
    }

    private boolean validateGenericDocument(Map<String, String> data) {
        log.debug("Validating generic document. Requirement: at least one field extracted");
        return !data.isEmpty();
    }
}
