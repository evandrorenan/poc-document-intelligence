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

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;

@Component
@Qualifier(VALIDATE_FIELD_CONTENT_ANALYZER)
@Slf4j
public class DocumentDataConsistencyChecker implements DocumentAnalyzerPort {

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {
        String referenceData = currentAnalysis.getReferenceData();
        String documentData = String.valueOf(currentAnalysis.getStepResults().get(AZURE_OPENAI_ANALYZER));

        currentAnalysis.getMatchParams().forEach(matchParam -> {

            List<String> fieldsToValidatePath = prepareFieldsToValidatePath(matchParam, referenceData);

            fieldsToValidatePath.forEach(fieldToValidatePath -> {
                try {
                    Object content = JsonPath.read(referenceData, fieldToValidatePath);
                    Object docContent = JsonPath.read(documentData, fieldToValidatePath);

                    if (!content.getClass().equals(docContent.getClass())) {
                        log.warn("Type doesn't match");
                        return;
                    }

                    if (isPrimitiveType(content) && !content.equals(docContent)) {
                        log.warn("Content doesn't match");
                        return;
                    }

                    if (isPrimitiveType(content)) {
                        log.info("MATCH {} filter: ", fieldToValidatePath);
                        return;
                    }

                    List<String> contentList = (List<String>) content;
                    List<String> docContentList = (List<String>) docContent;

                    List<String> unmatchedContent = new ArrayList<>(contentList);
                    contentList.forEach(item -> {
                        if (docContentList.contains(item    )) {
                            log.info("MATCH item: {} filter: ", fieldToValidatePath);
                            docContentList.remove(item);
                            unmatchedContent.remove(item);
                        }
                    });

                    logUnmatchedItems("Not found on doc: ", unmatchedContent);
                    logUnmatchedItems("Not found on user data: ", docContentList);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });


        return currentAnalysis;

    }

    private List<String> prepareFieldsToValidatePath(MatchParams matchParams, String referenceData) {
        List<String> fieldsToValidatePaths = new ArrayList<>();

        if (matchParams.getPathsToObjectKey().isEmpty()) {
            return List.of(matchParams.getPathToFieldContent());
        }

        String pathToObjectKey = matchParams.getPathsToObjectKey();
        Object objectKeys = JsonPath.read(referenceData, pathToObjectKey);

        if (isPrimitiveType(objectKeys)) {
            return List.of(matchParams.getPathToFieldContent());
        }

        if (objectKeys instanceof List<?> userContentObjectKeys) {
            userContentObjectKeys.forEach(key -> {
                String pathToContent = createFilter(matchParams.getPathToFieldContent(), pathToObjectKey, key);
                fieldsToValidatePaths.add(pathToContent);
            });
        }

        fieldsToValidatePaths.forEach(System.out::println);
        return fieldsToValidatePaths;
    }

    private boolean isPrimitiveType(Object object) {
        return object instanceof String || object instanceof Boolean || object instanceof Integer || object instanceof Double;
    }

    private String createFilter(String fieldContentJsonPath, String jsonPathKey, Object item) {
        int lastDotIndex = jsonPathKey.lastIndexOf(".");
        if (lastDotIndex == -1) return null;
        String fieldName = jsonPathKey.substring(lastDotIndex);

        String[] splitJsonPaths = fieldContentJsonPath.split("\\*", 2);
        if (splitJsonPaths.length < 2) return null;

        return splitJsonPaths[0] + "?(@" + fieldName + " == " + item + ")" + splitJsonPaths[1];
    }

    private void logUnmatchedItems(String message, List<String> items) {
        log.info(message);
        items.forEach(log::info);
    }

}
