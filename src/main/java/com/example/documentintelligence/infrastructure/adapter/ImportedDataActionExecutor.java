package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.FieldCheckRule;
import com.example.documentintelligence.domain.model.action.*;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.documentintelligence.domain.model.action.ActionResult.ActionOutcomeType.*;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.*;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.DOCUMENT_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;

@Component
@Qualifier(IMPORTED_DATA_ACTION_EXECUTOR)
public class ImportedDataActionExecutor implements DocumentAnalyzerPort {

    private static final String NO_OVERWRITE = "NO_OVERWRITE";
    private static final String OVERWRITE = "OVERWRITE";
    private final List<Action> actions;

    @Autowired
    public ImportedDataActionExecutor(List<Action> actions) {
        actions.sort(Comparator.comparingInt(action -> action.getActionType().getPriority()));
        this.actions = actions;
    }

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {
        ActionResult actionResult = new ActionResult();
        actionResult.setDocumentExtraData(new ArrayList<>());

        Object pathsFullMapObj = currentAnalysis.getStepResults().getOrDefault(
                VALIDATE_FIELD_CONTENT_ANALYZER + "Map", Collections.emptyMap());
        if (!(pathsFullMapObj instanceof Map<?, ?>)) {
            throw new IllegalStateException("Step result has an invalid type: " + pathsFullMapObj.getClass().toString());
        }
        var pathsFullMap = (Map<String, Map<FieldCheckRule, List<String>>>) pathsFullMapObj;

        List<ActionResult> actionResults = new ArrayList<>();
        this.actions.forEach(action -> {
            List<String> referencePaths = actionPaths(action, pathsFullMap.get(REFERENCE_PATHS));
            List<String> documentPaths = actionPaths(action, pathsFullMap.get(DOCUMENT_PATHS));
            String documentData = (String) currentAnalysis.getStepResults().getOrDefault(AZURE_OPENAI_ANALYZER, "");

            ActionResult result = action.execute(referencePaths, documentPaths, currentAnalysis.getReferenceData(), documentData);

            actionResults.add(result);
        });

        currentAnalysis.getStepResults().put(IMPORTED_DATA_ACTION_EXECUTOR, buildActionResult(actionResults));
        return currentAnalysis;
    }

    private ActionResult buildActionResult(List<ActionResult> actionResults) {
        return actionResults.stream().reduce((r1, r2) -> {
            if (r2 == null) return r1;
            r1.setSucceededActions(r1.getSucceededActions() + r2.getSucceededActions());
            r1.setFailedActions(r1.getFailedActions() + r2.getFailedActions());
            r1.getDocumentExtraData().addAll(r2.getDocumentExtraData());
            r1.setOutcomeType(defineOutcomeType(r1.getOutcomeType(), r2.getOutcomeType()));
            r1.setOutcome(mergeJson(r1.getOutcome(), r2.getOutcome()));
            return r1;
        }).orElseThrow(IllegalStateException::new);
    }

    private String mergeJson(String priorJson, String secondaryJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};
            Map<String, Object> priorMap = objectMapper.readValue(priorJson, typeReference);
            Map<String, Object> secondaryMap = objectMapper.readValue(secondaryJson, typeReference);

            mergeMaps(priorMap, secondaryMap);

            return objectMapper.writeValueAsString(priorMap);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void mergeMaps(Map<String, Object> priorMap, Map<String, Object> secondaryMap) {
        for (Map.Entry<String, Object> entry : secondaryMap.entrySet()) {
            mergeEntry(entry, priorMap, NO_OVERWRITE);
        }

        for (Map.Entry<String, Object> entry : priorMap.entrySet()) {
            mergeEntry(entry, secondaryMap, OVERWRITE);
        }
    }

    private static void mergeEntry(Map.Entry<String, Object> entry, Map<String, Object> secondaryMap, String mergeConfig) {
        String key = entry.getKey();
        Object priorValue = entry.getValue();

        if (isNestedMapMergeable(priorValue, secondaryMap, key)) {
            mergeNestedMaps(priorValue, secondaryMap, key);
            return;
        }

        if (isListMergeable(priorValue, secondaryMap, key)) {
            mergeNestedMapInList(priorValue, secondaryMap, key);
            return;
        }

        if (secondaryMap.containsKey(key) && mergeConfig.equalsIgnoreCase(NO_OVERWRITE)) {
            return;
        }

        overwriteSecondaryMap(secondaryMap, key, priorValue);
    }

    private static boolean isNestedMapMergeable(Object priorValue, Map<String, Object> secondaryMap, String key) {
        return priorValue instanceof Map && secondaryMap.containsKey(key) && secondaryMap.get(key) instanceof Map;
    }

    private static void mergeNestedMaps(Object priorValue, Map<String, Object> secondaryMap, String key) {
        mergeMaps((Map<String, Object>) priorValue, (Map<String, Object>) secondaryMap.get(key));
    }

    private static boolean isListMergeable(Object priorValue, Map<String, Object> secondaryMap, String key) {
        return priorValue instanceof List && secondaryMap.get(key) instanceof List;
    }

    private static void mergeNestedMapInList(Object priorValue, Map<String, Object> secondaryMap, String key) {
        List<?> priorList = (List<?>) priorValue;
        List<?> secondaryList = (List<?>) secondaryMap.get(key);

        if (!priorList.isEmpty() && !secondaryList.isEmpty() &&
                priorList.get(0) instanceof Map && secondaryList.get(0) instanceof Map) {
            mergeMaps((Map<String, Object>) priorList.get(0), (Map<String, Object>) secondaryList.get(0));
        }
    }

    private static void overwriteSecondaryMap(Map<String, Object> secondaryMap, String key, Object priorValue) {
        secondaryMap.put(key, priorValue);
    }

    private ActionResult.ActionOutcomeType defineOutcomeType(
            ActionResult.ActionOutcomeType outcomeType, ActionResult.ActionOutcomeType outcomeType1) {
        if (outcomeType.equals(SUCCESS)
        &&  outcomeType1.equals(SUCCESS)) {
            return SUCCESS;
        }

        if (outcomeType.equals(FAILURE)
        &&  outcomeType1.equals(FAILURE)) {
            return FAILURE;
        }

        if (outcomeType.equals(UNCHANGED)
        &&  outcomeType1.equals(UNCHANGED)) {
            return UNCHANGED;
        }

        return PARTIAL_SUCCESS;
    }

    private static List<String> actionPaths(Action action, Map<FieldCheckRule, List<String>> fieldsMap) {
        List<String> referencePaths = new ArrayList<>();
        fieldsMap.keySet().forEach(fieldCheckRule -> {
            if (fieldCheckRule.getAction().getClass().equals(action.getClass())) {
                referencePaths.addAll(fieldsMap.get(fieldCheckRule));
            }
        });
        return referencePaths;
    }
}
