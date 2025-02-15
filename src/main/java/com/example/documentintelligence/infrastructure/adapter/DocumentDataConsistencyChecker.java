package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.MatchParams;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;

@Component
@Qualifier(VALIDATE_FIELD_CONTENT_ANALYZER)
@Slf4j
public class DocumentDataConsistencyChecker implements DocumentAnalyzerPort {

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {
        var referenceData = currentAnalysis.getReferenceData();
        var documentData = String.valueOf(currentAnalysis.getStepResults().get(AZURE_OPENAI_ANALYZER));

        currentAnalysis.getMatchParams().forEach(matchParam ->
                validateFields(matchParam, referenceData, documentData));

        return currentAnalysis;
    }

    private void validateFields(MatchParams matchParam, String referenceData, String documentData) {
        var fieldsToValidate = prepareFieldsToValidate(matchParam, referenceData);

        fieldsToValidate.forEach(fieldPath ->
                validateField(fieldPath, referenceData, documentData));
    }

    private void validateField(String fieldPath, String referenceData, String documentData) {
        try {
            var referenceContent = JsonPath.read(referenceData, fieldPath);
            var documentContent = JsonPath.read(documentData, fieldPath);

            if (!areCompatibleTypes(referenceContent, documentContent)) {
                log.warn("Type mismatch for path: {}", fieldPath);
                return;
            }

            if (isPrimitiveType(referenceContent)) {
                validatePrimitiveContent(fieldPath, referenceContent, documentContent);
                return;
            }

            validateListContent(fieldPath, referenceContent, documentContent);

        } catch (Exception e) {
            log.error("Error validating field path: {}", fieldPath, e);
        }
    }

    private boolean areCompatibleTypes(Object reference, Object document) {
        return reference.getClass().equals(document.getClass());
    }

    private void validatePrimitiveContent(String fieldPath, Object reference, Object document) {
        if (reference.equals(document)) {
            log.info("Content match for path: {}", fieldPath);
        } else {
            log.warn("Content mismatch for path: {}", fieldPath);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateListContent(String fieldPath, Object reference, Object document) {
        var referenceList = new ArrayList<>((List<String>) reference);
        var documentList = new ArrayList<>((List<String>) document);
        var matchedItems = new ArrayList<String>();

        referenceList.forEach(item -> {
            if (documentList.contains(item)) {
                matchedItems.add(item);
                documentList.remove(item);
            }
        });
        referenceList.removeAll(matchedItems);

        if (!referenceList.isEmpty()) {
            log.info("Items not found in document for path {}: {}", fieldPath, referenceList);
        }
        if (!documentList.isEmpty()) {
            log.info("Extra items in document for path {}: {}", fieldPath, documentList);
        }
    }

    private List<String> prepareFieldsToValidate(MatchParams matchParams, String referenceData) {
        if (matchParams.getPathsToObjectKey().isEmpty()) {
            return List.of(matchParams.getPathToFieldContent());
        }

        var pathToObjectKey = matchParams.getPathsToObjectKey();
        var objectKeys = JsonPath.read(referenceData, pathToObjectKey);

        if (isPrimitiveType(objectKeys)) {
            return List.of(matchParams.getPathToFieldContent());
        }

        return createFieldPaths(matchParams, pathToObjectKey, objectKeys);
    }

    @SuppressWarnings("unchecked")
    private List<String> createFieldPaths(MatchParams matchParams, String pathToObjectKey, Object objectKeys) {
        var fieldPaths = new ArrayList<String>();

        if (objectKeys instanceof List<?> keys) {
            keys.stream()
                .map(key -> createFilter(matchParams.getPathToFieldContent(), pathToObjectKey, key))
                .filter(Objects::nonNull)
                .forEach(fieldPaths::add);
        }

        log.debug("Generated field paths: {}", fieldPaths);
        return fieldPaths;
    }

    private boolean isPrimitiveType(Object object) {
        return object instanceof String ||
                object instanceof Boolean ||
                object instanceof Integer ||
                object instanceof Double;
    }

    private String createFilter(String fieldContentJsonPath, String jsonPathKey, Object item) {
        var lastDotIndex = jsonPathKey.lastIndexOf(".");
        if (lastDotIndex == -1) return null;

        var fieldName = jsonPathKey.substring(lastDotIndex);
        var pathParts = fieldContentJsonPath.split("\\*", 2);

        if (pathParts.length < 2) return null;

        return String.format("%s?(@%s == %s)%s",
                pathParts[0], fieldName, item, pathParts[1]);
    }
}