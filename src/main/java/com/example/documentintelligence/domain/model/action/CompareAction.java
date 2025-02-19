package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.documentintelligence.domain.model.action.ActionResult.ActionOutcomeType.*;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.DOCUMENT_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.JsonPathProcessor.config;

@Slf4j
@Component
public class CompareAction implements Action {

    private static final String MATCH = "MATCH";
    private static final String ONLY_IN_REFERENCE = "ONLY_IN_REFERENCE";
    private static final String ONLY_IN_DOCUMENT = "ONLY_IN_DOCUMENT";
    private static final String ERROR_MISMATCH = "Informacao divergente do documento comprobatorio.";
    private static final String ERROR_NOT_FOUND = "Informacao nao encontrada no documento comprobatorio";

    @Override
    public ActionResult execute(DocumentAnalysis documentAnalysis) {
        ActionResult actionResult = new ActionResult();

        var stepResults = documentAnalysis.getStepResults();
        var validationResults = stepResults.getOrDefault(VALIDATE_FIELD_CONTENT_ANALYZER, Collections.emptyMap());

        if (!(validationResults instanceof Map<?, ?> validationMap) || validationMap.isEmpty()) {
            log.warn("Validation step produced invalid results: {}", validationResults);
            actionResult.setOutcomeType(UNCHANGED);
            return actionResult;
        }

        List<String> referencePaths = (List<String>) validationMap.get(REFERENCE_PATHS);
        List<String> documentPaths = (List<String>) validationMap.get(DOCUMENT_PATHS);

        Map<String, List<String>> pathComparison = comparePaths(referencePaths, documentPaths);

        String referenceData = documentAnalysis.getReferenceData();
        String documentData = (String) stepResults.getOrDefault(AZURE_OPENAI_ANALYZER, "");

        int pathErrors = validateMatchingPaths(pathComparison.get(MATCH), referenceData, documentData);
        int contentErrors = addErrorsToReferenceData(pathComparison.get(ONLY_IN_REFERENCE), referenceData, ERROR_NOT_FOUND);
        //* TODO: Implement behavior for ONLY_IN_DOCUMENT
        //*

        setActionResult(actionResult, pathComparison, pathErrors, contentErrors);

        return actionResult;
    }

    private static void setActionResult(ActionResult actionResult, Map<String, List<String>> pathComparison, int pathErrors, int contentErrors) {
        int failedActions = pathErrors + contentErrors;
        int succeededActions = pathComparison.get(MATCH).size();

        actionResult.setFailedActions(failedActions);
        actionResult.setSucceededActions(succeededActions);

        if (failedActions == 0 && succeededActions == 0) {
            actionResult.setOutcomeType(UNCHANGED);
            return;
        }

        actionResult.setOutcomeType((failedActions > 0 ^ succeededActions > 0) ? PARTIAL_SUCCESS : SUCCESS);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.COMPARE;
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

    private int validateMatchingPaths(List<String> matchingPaths, String referenceData, String documentData) {
        int errors = 0;
        for (String path : matchingPaths) {
            try {
                List<String> referenceValues = JsonPath.using(config).parse(referenceData).read(path);
                List<String> documentValues = JsonPath.using(config).parse(documentData).read(path);

                if (!referenceValues.get(0).equalsIgnoreCase(documentValues.get(0))) {
                    addErrorToReferenceData(path, referenceData, ERROR_MISMATCH);
                    matchingPaths.remove(path);
                    errors++;
                }
            } catch (PathNotFoundException e) {
                log.warn("Path not found in reference data: {}", path);
            }
        }
        return errors;
    }

    private int addErrorsToReferenceData(List<String> paths, String referenceData, String errorMessage) {
        for (String path : paths) {
            addErrorToReferenceData(path, referenceData, errorMessage);
        }
        return paths.size();
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
