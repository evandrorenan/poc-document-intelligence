package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.FieldCheckRule;
import com.example.documentintelligence.domain.model.MatchParams;
import com.example.documentintelligence.domain.model.action.Action;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.example.documentintelligence.domain.model.FieldCheckRule.ExpectedDataType.STRING;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.DOCUMENT_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;

@Slf4j
public class DocumentDataConsistencyCheckerTest {

    @Test
    void shouldValidateComplexArrayField() {
        // Given
        List<MatchParams> matchParams = getMatchParams();
        String referenceData = loadReferenceData();
        String documentData = loadDocumentData();

        DocumentAnalysis documentAnalysis = createDocumentAnalysis(referenceData, documentData, matchParams);

        // When
        DocumentAnalysis result = new DocumentDataConsistencyChecker().analyzeDocument(documentAnalysis);

        // Then
        Assertions.assertDoesNotThrow(() -> {
            Map<String, List<String>> pathsMap = (Map<String, List<String>>) result.getStepResults().get(VALIDATE_FIELD_CONTENT_ANALYZER);
            List<String> referencePaths = pathsMap.get(REFERENCE_PATHS);
            referencePaths.forEach(path -> JsonPath.parse(referenceData).read(path));
            List<String> documentPaths = pathsMap.get(DOCUMENT_PATHS);
            documentPaths.forEach(path -> JsonPath.parse(referenceData).read(path));
        });
    }


    private DocumentAnalysis createDocumentAnalysis(
            String referenceData,
            String documentData,
            List<MatchParams> matchParams) {

        Map<String, Object> stepResults = new HashMap<>();
        stepResults.put(AZURE_OPENAI_ANALYZER, documentData);
        return DocumentAnalysis.builder()
                               .referenceData(referenceData)
                               .stepResults(stepResults)
                               .matchParams(matchParams)
                               .build();
    }

    public static List<MatchParams> getMatchParams() {
        Map<String, String> pathsToObjectKeyMap = new LinkedHashMap<>(); //to preserver order
        pathsToObjectKeyMap.put("${MATRICULA}", "$.propriedadeUrbana[*].numeroMatricula");
        pathsToObjectKeyMap.put("${SEQ}", "$.propriedadeUrbana[?(@.numeroMatricula==${MATRICULA})].proprietarios[*].seq");

        Action action = null;

        FieldCheckRule baseFieldCheckRule = FieldCheckRule.builder()
                                                          .name("nome")
                                                          .friendlyName("Nome do proprietario")
                                                          .jsonPath("$.propriedadeUrbana[?(@.numeroMatricula==${MATRICULA})].proprietarios[?(@.seq==${SEQ})].nome")
                                                          .pathsToObjectKey(pathsToObjectKeyMap)
                                                          .expectedDataType(STRING)
                                                          .action(action)
                                                          .promptAdditionalInfo("Do not format. Content regex: [0-9a-zA-Z]{14}|[0-9a-zA-Z]{11}")
                                                          .build();

        return List.of(
                new MatchParams("$.propriedadeUrbana[?(@.numeroMatricula==${MATRICULA})].proprietarios[?(@.seq==${SEQ})].nome", List.of(baseFieldCheckRule)),
                new MatchParams("$.cpfCnpj", Collections.emptyList()));
    }

    public static String loadDocumentData() {
        return """
                    {
                      "cpfCnpj": "321",
                      "propriedadeUrbana": [
                        {
                          "proprietarios": [
                            {"seq": 0, "nome": "Claudia"},
                            {"seq": 1, "nome": "José"},
                            {"seq": 2, "nome": "Maria"},
                            {"seq": 3, "nome": "Eva"}
                          ],
                          "numeroMatricula": 123
                        }
                      ]
                    }
                """;
    }

    public static String loadReferenceData() {
        return """
                    {
                      "propriedadeUrbana": [
                        {
                          "proprietarios": [
                            {"seq": 0, "nome": "Maria"},
                            {"seq": 1, "nome": "José"},
                            {"seq": 3, "nome": "Benedita"}
                          ],
                          "numeroMatricula": 123
                        }
                      ],
                      "cpfCnpj": "321"
                    }
                """;
    }

}