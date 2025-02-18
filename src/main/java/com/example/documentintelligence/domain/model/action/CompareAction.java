package com.example.documentintelligence.domain.model.action;

import com.azure.json.models.JsonObject;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.*;
import static com.example.documentintelligence.infrastructure.adapter.JsonPathProcessor.config;

@Slf4j
public class CompareAction extends Action {

    public static final String MATCH = "MATCH";
    public static final String ONLY_ON_REF = "ONLY_ON_REF";
    public static final String ONLY_ON_DOC = "ONLY_ON_DOC";

    public CompareAction(Map<String, Object> actionParams) {
        super(ActionType.COMPARE, actionParams);
    }

    @Override
    public ActionResult execute(DocumentAnalysis currentAnalysis) {
        ActionResult actionResult = new ActionResult();

        var stepResults = currentAnalysis.getStepResults();
        var previousStepResults = stepResults.getOrDefault(VALIDATE_FIELD_CONTENT_ANALYZER, Collections.emptyMap());
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

        Map<String, List<String>> pathResults = compare(refPaths, docPaths);


        for (String path : pathResults.get(MATCH)) {
            List<String> refValues = JsonPath.using(config).parse(currentAnalysis.getReferenceData()).read(path);
            List<String> docValues = JsonPath.using(config).parse(documentData).read(path);
            Map<String, List<String>> results =
                    compare(refValues, docValues, currentAnalysis.getReferenceData(), documentData);

        }


        return null;
    }

    private Map<String, List<String>> compare(List<String> ref, List<String> target) {
        Map<String, List<String>> result = new HashMap<>();

//        List<String> onlyOnReferencePaths = ref.stream()
//                                               .filter(path -> !docSet.contains(path))
//                                               .toList();
//
//        List<String> onlyOnDocumentPaths = target.stream()
//                                                 .filter(path -> !refSet.contains(path))
//                                                 .toList();
//
//        List<String> matchPaths = ref.stream()
//                                     .filter(docSet::contains)
//                                     .toList();

//        result.put(MATCH, matchPaths);
//        result.put(ONLY_ON_REF, onlyOnReferencePaths);
//        result.put(ONLY_ON_DOC, onlyOnDocumentPaths);

        return result;
    }

    private Map<String, List<String>> compare(List<String> ref, List<String> target, String refJson, String targetJson) {
        Map<String, List<String>> result = new HashMap<>();

        Map<String, List<String>> pathCompareResult = compare(ref, target);
        List<String> matchPaths = pathCompareResult.get(MATCH);

        for (String path : matchPaths) {
            try {
                List<String> refValues = JsonPath.using(config).parse(refJson).read(path);
                if (!refValues.equals(refValues)) {
                    DocumentContext context = JsonPath.parse(refJson);
//                    List<JSONObject> values = context.read(path.substring(0, path.lastIndexOf('.')));
//                    for (JsonObject value : values) {
//                        String errorField = path.substring(path.lastIndexOf('.') + 1) + "Erro";
//                        value.setProperty(errorField, "Informacao divergente do documento comprobatorio.");
//                    }
//
//                    context.map(path, (obj, configuration) -> {
//                        new JsonObject()
//
//                        ((JsonObject) obj).put(errorField, "Informacao divergente do documento comprobatorio.");
//                        return obj;
//                    });
                }
            } catch (PathNotFoundException e) {
                log.warn("Path not found on reference data: {}", path);
            }

            List<String> docValues = JsonPath.using(config).parse(targetJson).read(path);



        }


//        List<String> onlyOnReferencePaths = ref.stream()
//                                               .filter(path -> !docSet.contains(path))
//                                               .toList();
//
//        List<String> onlyOnDocumentPaths = target.stream()
//                                                 .filter(path -> !refSet.contains(path))
//                                                 .toList();

        result.put(MATCH, matchPaths);
//        result.put(ONLY_ON_REF, onlyOnReferencePaths);
//        result.put(ONLY_ON_DOC, onlyOnDocumentPaths);

        return result;
    }
}
