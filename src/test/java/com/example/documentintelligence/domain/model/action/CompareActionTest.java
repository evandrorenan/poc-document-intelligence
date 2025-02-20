package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.DOCUMENT_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareActionTest {

    @Test
    void getActionType_ShouldReturnCompare() {
        assertEquals(Action.ActionType.COMPARE, new CompareAction().getActionType());
    }

    @Test
    void execute_ShouldReturnFailure_WhenValidationResultsAreInvalid() {
        DocumentAnalysis documentAnalysis = mock(DocumentAnalysis.class);
        when(documentAnalysis.getStepResults()).thenReturn(Map.of(VALIDATE_FIELD_CONTENT_ANALYZER, "invalid"));
        ActionResult result = new CompareAction().execute(documentAnalysis);
        assertEquals(ActionResult.ActionOutcomeType.FAILURE, result.getOutcomeType());
    }

    @Test
    void execute_ShouldHandleEmptyValidationResults() {
        DocumentAnalysis documentAnalysis = mock(DocumentAnalysis.class);
        when(documentAnalysis.getStepResults()).thenReturn(Map.of(VALIDATE_FIELD_CONTENT_ANALYZER, Map.of()));
        ActionResult result = new CompareAction().execute(documentAnalysis);
        assertEquals(ActionResult.ActionOutcomeType.FAILURE, result.getOutcomeType());
    }

    @Test
    void execute_ShouldComparePathsCorrectly() {
        DocumentAnalysis documentAnalysis = mock(DocumentAnalysis.class);
        Map<String, Object> validationResults = Map.of(
                REFERENCE_PATHS, List.of("$.field1", "$.field2"),
                DOCUMENT_PATHS, List.of("$.field1", "$.field3")
        );

        when(documentAnalysis.getStepResults()).thenReturn(Map.of(
                VALIDATE_FIELD_CONTENT_ANALYZER, validationResults,
                AZURE_OPENAI_ANALYZER, "{\"field1\": \"value1\", \"field3\": \"value3\"}"));
        when(documentAnalysis.getReferenceData()).thenReturn("{\"field1\": \"value1\", \"field2\": \"value2\"}");

        ActionResult result = new CompareAction().execute(documentAnalysis);
        assertNotNull(result);
        assertEquals(ActionResult.ActionOutcomeType.PARTIAL_SUCCESS, result.getOutcomeType());
    }

    @Test
    void execute_ShouldReturnSuccess_WhenAllPathsMatch() {
        DocumentAnalysis documentAnalysis = mock(DocumentAnalysis.class);
        Map<String, Object> validationResults = Map.of(
                REFERENCE_PATHS, List.of("$.field1"),
                DOCUMENT_PATHS, List.of("$.field1")
        );

        when(documentAnalysis.getStepResults()).thenReturn(Map.of(
                VALIDATE_FIELD_CONTENT_ANALYZER, validationResults,
                AZURE_OPENAI_ANALYZER, "{\"field1\": \"value1\"}"));
        when(documentAnalysis.getReferenceData()).thenReturn("{\"field1\": \"value1\"}");

        ActionResult result = new CompareAction().execute(documentAnalysis);
        assertNotNull(result);
        assertEquals(ActionResult.ActionOutcomeType.SUCCESS, result.getOutcomeType());
    }

    @Test
    void execute_ShouldReturnPartialSuccess_WhenPathsMismatch() {
        DocumentAnalysis documentAnalysis = mock(DocumentAnalysis.class);
        Map<String, Object> validationResults = Map.of(
                REFERENCE_PATHS, List.of("$.field1"),
                DOCUMENT_PATHS, List.of("$.field1")
        );

        when(documentAnalysis.getStepResults()).thenReturn(Map.of(
                VALIDATE_FIELD_CONTENT_ANALYZER, validationResults,
                AZURE_OPENAI_ANALYZER, "{\"field1\": \"value2\"}"));

        when(documentAnalysis.getReferenceData()).thenReturn("{\"field1\": \"value1\"}");

        ActionResult result = new CompareAction().execute(documentAnalysis);
        assertNotNull(result);
        assertEquals(ActionResult.ActionOutcomeType.PARTIAL_SUCCESS, result.getOutcomeType());
    }
}
