package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.documentintelligence.domain.model.action.ActionResult.ActionOutcomeType.*;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.*;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.DOCUMENT_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.JsonPathProcessor.config;

@Slf4j
@Component
public class OverrideAction extends Action {

    private final ActionType actionType = ActionType.OVERRIDE;

    public OverrideAction() {
        super();
    }

    @Override
    @JsonProperty("actionType")
    public ActionType getActionType() {
        return actionType;
    }

    @Override
    public ActionResult execute(List<String> referencePaths, List<String> documentPaths, String referenceData, String documentData) {
        ActionResult actionResult = new ActionResult();
        actionResult.setDocumentExtraData(new ArrayList<>());
        actionResult.setOutcome(referenceData);

        int overriddenPaths = overridePaths(referencePaths, documentPaths, documentData, actionResult);

        setActionResult(actionResult, overriddenPaths);

        return actionResult;
    }

    public ActionResult execute(DocumentAnalysis documentAnalysis) {
        ActionResult actionResult = new ActionResult();
        actionResult.setDocumentExtraData(new ArrayList<>());

        Map<String, Object> validationResults = getValidationResults(documentAnalysis);

        if (validationResults.isEmpty()) {
            return handleEmptyValidationResults(actionResult);
        }

        List<String> referencePaths = (List<String>) validationResults.get(REFERENCE_PATHS);
        List<String> documentPaths = (List<String>) validationResults.get(DOCUMENT_PATHS);

        String referenceData = documentAnalysis.getReferenceData();
        String documentData = (String) documentAnalysis.getStepResults().getOrDefault(AZURE_OPENAI_ANALYZER, "");

        actionResult.setOutcome(referenceData);

        int overriddenPaths = overridePaths(referencePaths, documentPaths, documentData, actionResult);

        setActionResult(actionResult, overriddenPaths);

        documentAnalysis.getStepResults().put(IMPORTED_DATA_ACTION_EXECUTOR, actionResult);

        return actionResult;
    }

    private Map<String, Object> getValidationResults(DocumentAnalysis documentAnalysis) {
        var validationResults = documentAnalysis.getStepResults().getOrDefault(
                VALIDATE_FIELD_CONTENT_ANALYZER, Collections.emptyMap());

        if (!(validationResults instanceof Map<?, ?>)) {
            log.warn("Validation step didn't produce a valid Map");
            return Collections.emptyMap();
        }

        return (Map<String, Object>) validationResults;
    }

    private ActionResult handleEmptyValidationResults(ActionResult actionResult) {
        log.warn("Validation step produced invalid results");
        actionResult.setOutcomeType(FAILURE);
        return actionResult;
    }

    private int overridePaths(List<String> referencePaths, List<String> documentPaths, String documentData, ActionResult actionResult) {
        int overridden = 0;
        for (String path : documentPaths) {
            overridden += overridePath(path, documentData, actionResult);
        }
        return overridden;
    }

        private int overridePath(String path, String documentData, ActionResult actionResult) {
        String referenceData = actionResult.getOutcome();
        try {
            DocumentContext docContext = JsonPath.using(config).parse(documentData);
            DocumentContext refContext = JsonPath.using(config).parse(referenceData);

            List<Object> docValues = docContext.read(path);
            if (docValues.isEmpty()) {
                return 0;
            }

            Object value = docValues.get(0);

            try {
                refContext.set(path, value);
            } catch (PathNotFoundException e) {
                createOrUpdatePath(refContext, path, value);
            }

            actionResult.setOutcome(refContext.jsonString());
            return 1;
        } catch (PathNotFoundException e) {
            log.warn("Override error. Path not found: {}", path);
            return 0;
        }
    }

    private void createOrUpdatePath(DocumentContext refContext, String path, Object value) {
        try {
            refContext.set(path, value);
        } catch (PathNotFoundException e) {
            createParentNodes(refContext, path, value);
        }
    }

    private void createParentNodes(DocumentContext refContext, String path, Object value) {
        String[] parts = path.split("\\.");
        if (parts.length <= 1) {
            log.error("Invalid path: {}", path);
            return;
        }

        String currentPath = "$";
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            String nextPath = currentPath + "." + part;

            if (part.contains("[") && part.contains("]")) { // Array handling
                String arrayName = part.substring(0, part.indexOf("["));
                String arrayPath = currentPath + "." + arrayName;

                try {
                    refContext.read(arrayPath);
                } catch (PathNotFoundException e) {
                    refContext.put(currentPath, arrayName, new ArrayList<>());
                }

                if (i == parts.length - 1) { // Last part, set the value
                    try {
                        refContext.set(path, value);
                    } catch (PathNotFoundException e) {
                        try {
                            //If the filter does not match, create a new item.
                            List<Object> array = refContext.read(arrayPath);
                            HashMap<String, Object> newItem = new HashMap<>();
                            String field = part.substring(part.lastIndexOf("]") + 2);
                            newItem.put(field, value);
                            array.add(newItem);
                            refContext.set(arrayPath, array);
                        }catch(Exception e2){
                            log.error("Error creating new array item.", e2);
                        }
                    }
                } else {
                    currentPath = nextPath;
                }

            } else { // Object handling
                try {
                    refContext.read(nextPath);
                    currentPath = nextPath;
                } catch (PathNotFoundException e) {
                    if (i == parts.length - 1) { // Last part, set the value
                        refContext.put(currentPath, part, value);
                    } else {
                        refContext.put(currentPath, part, new HashMap<>());
                        currentPath = nextPath;
                    }
                }
            }
        }
    }

    private void setActionResult(ActionResult actionResult, int overriddenPaths) {
        actionResult.setFailedActions(0);
        actionResult.setSucceededActions(overriddenPaths);
        actionResult.setOutcomeType(overriddenPaths > 0 ? SUCCESS : UNCHANGED);
    }
}