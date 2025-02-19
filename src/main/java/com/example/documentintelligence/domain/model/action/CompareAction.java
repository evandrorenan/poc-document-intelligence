package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

import java.util.*;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.DOCUMENT_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.JsonPathProcessor.config;

@Slf4j
public class CompareAction extends Action {

    private static final String MATCH = "MATCH";
    private static final String ONLY_IN_REFERENCE = "ONLY_IN_REFERENCE";
    private static final String ONLY_IN_DOCUMENT = "ONLY_IN_DOCUMENT";
    private static final String ERROR_MISMATCH = "Informacao divergente do documento comprobatorio.";
    private static final String ERROR_NOT_FOUND = "Informacao nao encontrada no documento comprobatorio";

    public CompareAction(Map<String, Object> actionParams) {
        super(ActionType.COMPARE, actionParams);
    }

    @Override
    public ActionResult execute(DocumentAnalysis documentAnalysis) {
        ActionResult actionResult = new ActionResult();

        var stepResults = documentAnalysis.getStepResults();
        var validationResults = stepResults.getOrDefault(VALIDATE_FIELD_CONTENT_ANALYZER, Collections.emptyMap());

        if (!(validationResults instanceof Map<?, ?> validationMap) || validationMap.isEmpty()) {
            log.warn("Validation step produced invalid results: {}", validationResults);
            actionResult.setMessage("Não foi possível validar o campo: " + validationResults);
            return actionResult;
        }

        List<String> referencePaths = (List<String>) validationMap.get(REFERENCE_PATHS);
        List<String> documentPaths = (List<String>) validationMap.get(DOCUMENT_PATHS);

        Map<String, List<String>> pathComparison = comparePaths(referencePaths, documentPaths);

        String referenceData = documentAnalysis.getReferenceData();
        String documentData = (String) stepResults.getOrDefault(AZURE_OPENAI_ANALYZER, "");

        validateMatchingPaths(pathComparison.get(MATCH), referenceData, documentData);
        addErrorsToReferenceData(pathComparison.get(ONLY_IN_REFERENCE), referenceData, ERROR_NOT_FOUND);
        // TODO: Implement behavior for ONLY_IN_DOCUMENT

        return actionResult;
    }

    private Map<String, List<String>> comparePaths(List<String> referencePaths, List<String> documentPaths) {
        Map<String, List<String>> result = new HashMap<>();
        Set<String> referenceSet = new HashSet<>(referencePaths);
        Set<String> documentSet = new HashSet<>(documentPaths);

        result.put(MATCH, referencePaths.stream().filter(documentSet::contains).toList());
        result.put(ONLY_IN_REFERENCE, referencePaths.stream().filter(path -> !documentSet.contains(path)).toList());
        result.put(ONLY_IN_DOCUMENT, documentPaths.stream().filter(path -> !referenceSet.contains(path)).toList());

        return result;
    }

    private void validateMatchingPaths(List<String> matchingPaths, String referenceData, String documentData) {
        for (String path : matchingPaths) {
            try {
                List<String> referenceValues = JsonPath.using(config).parse(referenceData).read(path);
                List<String> documentValues = JsonPath.using(config).parse(documentData).read(path);

                if (!referenceValues.get(0).equalsIgnoreCase(documentValues.get(0))) {
                    addErrorToReferenceData(path, referenceData, ERROR_MISMATCH);
                }
            } catch (PathNotFoundException e) {
                log.warn("Path not found in reference data: {}", path);
            }
        }
    }

    private void addErrorsToReferenceData(List<String> paths, String referenceData, String errorMessage) {
        for (String path : paths) {
            addErrorToReferenceData(path, referenceData, errorMessage);
        }
    }

    private void addErrorToReferenceData(String path, String referenceData, String errorMessage) {
        try {
            int lastIndex = Math.max(path.lastIndexOf('.'), path.lastIndexOf(']'));
            String parentPath = path.substring(0, lastIndex);
            String fieldName = path.substring(lastIndex + 1);

            JSONArray contentArray = JsonPath.using(config).parse(referenceData).json();
            Map targetNode = (LinkedHashMap) JsonPath.using(config).parse(contentArray).read(parentPath, List.class).get(0);
            targetNode.put(fieldName + "Erro", errorMessage);
        } catch (PathNotFoundException e) {
            log.warn("Path not found in reference data: {}", path);
        }
    }
}
