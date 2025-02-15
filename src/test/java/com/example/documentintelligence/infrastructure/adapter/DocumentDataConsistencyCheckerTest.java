package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.MatchParams;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;

class DocumentDataConsistencyCheckerTest {

    @Test
    void shouldValidateComplexArrayField() {
        // Given

        String referenceData = loadReferenceData();

        String documentData = loadDocumentData();

        List<MatchParams> matchParams = List.of(
                new MatchParams("$.propriedadeUrbana[*].proprietarios[*].nome",
                        "$.propriedadeUrbana[*].numeroMatricula"),
                new MatchParams("$.cpfCnpj", ""));

        DocumentAnalysis documentAnalysis = createDocumentAnalysis(referenceData, documentData, matchParams);

        // When
        DocumentAnalysis result = new DocumentDataConsistencyChecker().analyzeDocument(documentAnalysis);

        // Then


    }

    private static String loadDocumentData() {
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

    private static String loadReferenceData() {
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