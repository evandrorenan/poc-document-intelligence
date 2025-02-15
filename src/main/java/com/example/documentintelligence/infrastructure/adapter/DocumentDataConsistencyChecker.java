package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;

@Component
@Qualifier(VALIDATE_FIELD_CONTENT_ANALYZER)
@Slf4j
public class DocumentDataConsistencyChecker  implements DocumentAnalyzerPort {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {

            //bring code from scratch to here...

            return currentAnalysis;

    }

}
