package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.FieldCheckRule;
import com.example.documentintelligence.domain.model.MatchParams;
import com.example.documentintelligence.domain.model.action.Action;
import com.example.documentintelligence.domain.model.action.CompareAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.example.documentintelligence.domain.model.FieldCheckRule.ExpectedDataType.STRING;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.JsonPathProcessor.config;

@Slf4j
public class DocumentDataConsistencyCheckerTest {

    @Test
    void shouldValidateComplexArrayField() throws JsonProcessingException {
        // Given
        List<MatchParams> matchParams = getMatchParams();

        String referenceData = loadReferenceData();
        String documentData = loadDocumentData();

        DocumentAnalysis documentAnalysis = createDocumentAnalysis(referenceData, documentData, matchParams);

        // When
        DocumentAnalysis result = new DocumentDataConsistencyChecker().analyzeDocument(documentAnalysis);

        // Then


    }

    public static List<MatchParams> getMatchParams() throws JsonProcessingException {
        Map<String, String> pathsToObjectKeyMap = new LinkedHashMap<>(); //to preserver order
        pathsToObjectKeyMap.put("${MATRICULA}", "$.propriedadeUrbana[*].numeroMatricula");
        pathsToObjectKeyMap.put("${SEQ}", "$.propriedadeUrbana[?(@.numeroMatricula==${MATRICULA})].proprietarios[*].seq");

        Action action = new CompareAction(Map.of("ERROR_MESSAGE", "Imóvel ${MATRICULA} não encontrado"));

        FieldCheckRule baseFieldCheckRule = FieldCheckRule.builder()
                                                          .name("nome")
                                                          .friendlyName("Nome do proprietario")
                                                          .jsonPath("$.propriedadeUrbana[?(@.numeroMatricula==${MATRICULA})].proprietarios[?(@.seq==${SEQ})].nome")
                                                          .pathsToObjectKey(pathsToObjectKeyMap)
                                                          .expectedDataType(STRING)
                                                          .action(action)
                                                          .promptAdditionalInfo("Do not format. Content regex: [0-9a-zA-Z]{14}|[0-9a-zA-Z]{11}")
                                                          .build();

        List<MatchParams> matchParams = List.of(
                new MatchParams("$.propriedadeUrbana[?(@.numeroMatricula==${MATRICULA})].proprietarios[?(@.seq==${SEQ})].nome", List.of(baseFieldCheckRule)),
                new MatchParams("$.cpfCnpj", Collections.emptyList()));

        String matchParamsJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(matchParams);


        JSONArray jsonArray = JsonPath.using(config).parse(matchParamsJson).json();
        Map targetNode = (LinkedHashMap) JsonPath.using(config).parse(jsonArray).read("$[0].fieldCheckRules[0]", List.class).get(0);
        targetNode.put("idade", 10);
        new ObjectMapper().writeValueAsString(targetNode);


        log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(""));

        matchParams.forEach(mp -> {
            try {
                log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(matchParams));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return matchParams;
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
}