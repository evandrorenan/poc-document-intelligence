package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;

@Component
@Qualifier(VALIDATE_FIELD_CONTENT_ANALYZER)
@Slf4j
public class DocumentDataConsistencyChecker implements DocumentAnalyzerPort {

    public static final String REFERENCE_PATHS = "REFERENCE_PATHS";
    public static final String DOCUMENT_PATHS = "DOCUMENT_PATHS";

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {
        var referenceData = currentAnalysis.getReferenceData();
        var documentData = String.valueOf(currentAnalysis.getStepResults().get(AZURE_OPENAI_ANALYZER));

        List<String> referencePaths = expandPaths(currentAnalysis, referenceData);
        List<String> documentPaths = expandPaths(currentAnalysis, documentData);

        Map<String, List<String>> pathsMap = Map.of(REFERENCE_PATHS, referencePaths, DOCUMENT_PATHS, documentPaths);
        currentAnalysis.getStepResults()
                       .put(VALIDATE_FIELD_CONTENT_ANALYZER,
                               pathsMap);

        return currentAnalysis;
    }

    private static List<String> expandPaths(DocumentAnalysis currentAnalysis, String referenceData) {
        try {
            List<String> strings = currentAnalysis.getDocumentValidationRule().getFieldsToCheck().stream().flatMap(fieldCheckRule -> {
                Map<String, String> pendingPaths = new LinkedHashMap<>(fieldCheckRule.getPathsToObjectKey());
                Stream<String> stream = JsonPathProcessor.replaceTokens(fieldCheckRule.getJsonPath(), referenceData, pendingPaths).stream();
                return stream;
            }).toList();
            return strings;
        } catch (JsonPathException e) {
            return List.of("$");
        }
    }
}