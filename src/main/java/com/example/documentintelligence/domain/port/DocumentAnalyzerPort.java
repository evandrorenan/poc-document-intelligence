package com.example.documentintelligence.domain.port;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.DocumentType;
import com.example.documentintelligence.domain.workflow.DocumentProcessingState;

public interface DocumentAnalyzerPort {

    DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis);
}
