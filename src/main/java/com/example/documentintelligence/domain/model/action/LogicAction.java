package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.FieldCheckRule;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.documentintelligence.domain.model.action.ActionResult.ActionOutcomeType.*;
import static com.example.documentintelligence.domain.model.action.ActionResult.ActionOutcomeType.PARTIAL_SUCCESS;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.JsonPathProcessor.config;

@Slf4j
@Component
public class LogicAction extends Action {
    public static class LogicExtraInfo {
        private String logicExpression;

        public String getLogicExpression() {
            return logicExpression;
        }

        public LogicExtraInfo setLogicExpression(String logicExpression) {
            this.logicExpression = logicExpression;
            return this;
        }
    }

    public LogicAction() {
        this.extraInfo = new LogicExtraInfo();
    }


    /*
 Matrícula precisa ter certificação negativa de ônus. Como identificar? (valid = true/false)
 Selo de registro. (valid = true/false)
 Cpf Cnpj do proprietario COMPARE
 Cidade COMPARE
 Area COMPARE
 Unidade de medida COMPARE
 Comarca COMPARE
 Nome dos proprietários COMPARE
 Data emissao LOGIC
 Tipo de imóvel
 UF



  */
    @Override
    public ActionType getActionType() {
        return ActionType.LOGIC;
    }

    @Override
    public ActionResult execute(DocumentAnalysis documentAnalysis) {

        LogicExtraInfo logicExtraInfo =
            (LogicExtraInfo) documentAnalysis.getDocumentValidationRule()
                 .getFieldsToCheck().stream().map(f -> f.getExtraInfo());

        ActionResult actionResult = new ActionResult();
        actionResult.setDocumentExtraData(new ArrayList<>());

        Map<String, Map<FieldCheckRule, List<String>>> validationResults = getValidationResults(documentAnalysis);

        if (validationResults.isEmpty()) {
            return handleEmptyValidationResults(actionResult);
        }

        List<String> referencePaths = (List<String>) validationResults.get(REFERENCE_PATHS);
        String referenceData = documentAnalysis.getReferenceData();

        for (String referencePath : referencePaths) {
            documentAnalysis.getDocumentValidationRule().getFieldsToCheck()
        }

        actionResult.setOutcome(referenceData);





    }

    private Map<String, Map<FieldCheckRule, List<String>>> getValidationResults(DocumentAnalysis documentAnalysis) {
        var validationResults = documentAnalysis.getStepResults().getOrDefault(
                VALIDATE_FIELD_CONTENT_ANALYZER + "Map", Collections.emptyMap());

        if (!(validationResults instanceof Map<?, ?>)) {
            log.warn("Validation step didn't produce a valid Map");
            return Collections.emptyMap();
        }

        return (Map<String, Map<FieldCheckRule, List<String>>>) validationResults;
    }

    private ActionResult handleEmptyValidationResults(ActionResult actionResult) {
        log.warn("Validation step produced invalid results");
        actionResult.setOutcomeType(FAILURE);
        return actionResult;
    }


    private Map<String, List<String>> comparePaths(List<String> referencePaths, List<String> documentPaths) {
        Map<String, List<String>> result = new HashMap<>();
        Set<String> referenceSet = new HashSet<>(referencePaths);
        Set<String> documentSet = new HashSet<>(documentPaths);

        result.put(MATCH, referencePaths.stream().filter(documentSet::contains).toList());
        result.put(ONLY_ON_REFERENCE, referencePaths.stream().filter(p -> !documentSet.contains(p)).toList());
        result.put(ONLY_ON_DOCUMENT, documentPaths.stream().filter(p -> !referenceSet.contains(p)).toList());
        return result;
    }

    private int validateMatchingPaths(List<String> matchingPaths, String documentData, ActionResult actionResult) {
        int errors = 0;
        for (String path : matchingPaths) {
            errors += validatePath(path, documentData, actionResult);
        }
        return errors;
    }

    private int validatePath(String path, String documentData, ActionResult actionResult) {
        var referenceData = actionResult.getOutcome();
        try {
            List<String> refValues = JsonPath.using(config).parse(referenceData).read(path);
            List<String> docValues = JsonPath.using(config).parse(documentData).read(path);

            if (refValues.size() == 0) {
                actionResult.getDocumentExtraData().add(path + ": " + docValues);
                return 1;
            }

            if (docValues.size() == 0) {
                referenceData = addErrorToReferenceData(path, ERROR_NOT_FOUND, actionResult);
                actionResult.setOutcome(referenceData);
                return 1;
            }

            if (!refValues.get(0).equalsIgnoreCase(docValues.get(0))) {
                referenceData = addErrorToReferenceData(path, ERROR_MISMATCH, actionResult);
                actionResult.setOutcome(referenceData);
                return 1;
            }
        } catch (PathNotFoundException e) {
            log.warn("Validate error. Path not found: {}", path);
        }
        return 0;
    }


    private int addErrorsToReferenceData(List<String> paths, String errorMessage, ActionResult actionResult) {
        int count = 0;
        for (String path : paths) {
            actionResult.setOutcome(addErrorToReferenceData(path, errorMessage, actionResult));
            count++;
        }
        return count;
    }

    private String addErrorToReferenceData(String path, String errorMessage, ActionResult actionResult) {
        String referenceData = actionResult.getOutcome();

        try {
            int lastIndex = Math.max(path.lastIndexOf('.'), path.lastIndexOf(']'));
            String parentPath = path.substring(0, lastIndex);
            String fieldName = path.substring(lastIndex + 1);

            var documentContext = JsonPath.using(config).parse(referenceData);
            List<Map<String, Object>> nodes = documentContext.read(parentPath);
            if (nodes.isEmpty()) {
                throw new PathNotFoundException("Path not found: " + parentPath);
            }

            Map<String, Object> targetNode = nodes.get(0);
            targetNode.put(fieldName + "Erro", errorMessage);

            documentContext.set(parentPath, nodes);
            referenceData = documentContext.jsonString();

        } catch (PathNotFoundException e) {
            log.warn("Could'nt add error to reference data. Path not found: {}", path);
        }
        return referenceData;
    }


    private void setActionResult(ActionResult actionResult, Map<String, List<String>> pathComparison, int pathErrors, int contentErrors) {
        int failed = pathErrors + contentErrors;
        int succeeded = pathComparison.get(MATCH).size();

        actionResult.setFailedActions(failed);
        actionResult.setSucceededActions(succeeded);
        actionResult.setOutcomeType(determineOutcome(failed, succeeded));
    }

    private ActionResult.ActionOutcomeType determineOutcome(int failed, int succeeded) {
        if (failed == 0 && succeeded == 0) return UNCHANGED;
        if (failed == 0 && succeeded == 1) return SUCCESS;
        if (failed > 0 && succeeded > 0) return PARTIAL_SUCCESS;
        return FAILURE; // Covers failed > 0 and succeeded == 0, and failed > 1 and succeeded > 0
    }
}