package com.example.documentintelligence.domain.port;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.DocumentType;

public interface DocumentAnalyzerPort {
    DocumentAnalysis analyzeDocument(String base64Document, DocumentType documentType);
}
