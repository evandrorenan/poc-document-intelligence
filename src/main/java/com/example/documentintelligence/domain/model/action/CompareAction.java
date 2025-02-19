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

    public static final String MATCH = "MATCH";
    public static final String ONLY_ON_REF = "ONLY_ON_REF";
    public static final String ONLY_ON_DOC = "ONLY_ON_DOC";
    public static final String INFORMACAO_DIVERGENTE_DO_DOCUMENTO_COMPROBATORIO = "Informacao divergente do documento comprobatorio.";
    public static final String INFORMACAO_NAO_ENCONTRADA_NO_DOCUMENTO_COMPROBATORIO = "Informacao nao encontrada no documento comprobatorio";

    public CompareAction(Map<String, Object> actionParams) {
        super(ActionType.COMPARE, actionParams);
    }

    @Override
    public ActionResult execute(DocumentAnalysis currentAnalysis) {
        ActionResult actionResult = new ActionResult();

        var stepResults = currentAnalysis.getStepResults();
        var previousStepResults = stepResults.getOrDefault(VALIDATE_FIELD_CONTENT_ANALYZER, Collections.emptyMap());

        String referenceData = currentAnalysis.getReferenceData();
        String documentData = (String) stepResults.getOrDefault(AZURE_OPENAI_ANALYZER, "");

        if ((!(previousStepResults instanceof Map<?, ?>))) {
            log.warn("Validation step produced an invalid object.: {}", previousStepResults);
            actionResult.setMessage("Não foi possivel validar o campo: {}" + previousStepResults);
            return actionResult;
        }

        var previousStepResultsMap = (Map<String, List<String>>) previousStepResults;
        if (((Map) previousStepResults).isEmpty()) {
            log.warn("Validation step didn't generate valid inputs: {}", previousStepResults);
            actionResult.setMessage("Não foi possivel validar o campo: {}" + previousStepResults);
            return actionResult;
        }

        List<String> refPaths = previousStepResultsMap.get(REFERENCE_PATHS);
        List<String> docPaths = previousStepResultsMap.get(DOCUMENT_PATHS);

        Map<String, List<String>> pathCompareResults = compare(refPaths, docPaths);

        //MATCH
        for (String path : pathCompareResults.get(MATCH)) {
            List<String> refValues = JsonPath.using(config).parse(referenceData).read(path);
            List<String> docValues = JsonPath.using(config).parse(documentData).read(path);

            if (String.valueOf(refValues.get(0)).equalsIgnoreCase(String.valueOf(docValues.get(0)))) {
                Map<String, List<String>> contentCompareResults = compare(refValues, docValues);

                List<String> matchedPaths = contentCompareResults.get(MATCH);

                for (String matchedPath : matchedPaths) {
                    try {
                        int fieldNameInit = Math.max(matchedPath.lastIndexOf('.'), matchedPath.lastIndexOf(']'));
                        String parentPath = matchedPath.substring(0, fieldNameInit);
                        String fieldName = matchedPath.substring(fieldNameInit + 1);

                        JSONArray contentArray = JsonPath.using(config).parse(referenceData).json();
                        Map targetNode = (LinkedHashMap) JsonPath.using(config).parse(contentArray).read(parentPath, List.class).get(0);
                        targetNode.put(fieldName + "Erro", INFORMACAO_DIVERGENTE_DO_DOCUMENTO_COMPROBATORIO);

                    } catch (PathNotFoundException e) {
                        log.warn("Path not found on reference data: {}", path);
                    }
                }
            }
        }

        //ONLY ON REF
        for (String path : pathCompareResults.get(ONLY_ON_REF)) {
            try {
                int fieldNameInit = Math.max(path.lastIndexOf('.'), path.lastIndexOf(']'));
                String parentPath = path.substring(0, fieldNameInit);
                String fieldName = path.substring(fieldNameInit + 1);

                JSONArray contentArray = JsonPath.using(config).parse(referenceData).json();
                Map targetNode = (LinkedHashMap) JsonPath.using(config).parse(contentArray).read(parentPath, List.class).get(0);
                targetNode.put(fieldName + "Erro", INFORMACAO_NAO_ENCONTRADA_NO_DOCUMENTO_COMPROBATORIO);

            } catch (PathNotFoundException e) {
                log.warn("Path not found on reference data: {}", path);
            }
        }

        //ONLY ON DOCUMENT
        //TODO: Implement behavior for data found just on document
        return actionResult;
    }

    private Map<String, List<String>> compare(List<String> ref, List<String> target) {
        Map<String, List<String>> result = new HashMap<>();

        Set<String> refSet = new HashSet<>(ref);
        Set<String> docSet = new HashSet<>(target);

        List<String> onlyOnReferencePaths = ref.stream()
                                               .filter(path -> !docSet.contains(path))
                                               .toList();

        List<String> onlyOnDocumentPaths = target.stream()
                                                 .filter(path -> !refSet.contains(path))
                                                 .toList();

        List<String> matchPaths = ref.stream()
                                     .filter(docSet::contains)
                                     .toList();

        result.put(MATCH, matchPaths);
        result.put(ONLY_ON_REF, onlyOnReferencePaths);
        result.put(ONLY_ON_DOC, onlyOnDocumentPaths);

        return result;
    }
}
