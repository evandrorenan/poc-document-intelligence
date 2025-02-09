package com.example.documentintelligence.domain.workflow;

import com.example.documentintelligence.domain.model.DocumentAnalysis;

public interface DocumentStateProcessor {

    Boolean checkPreReqs(DocumentAnalysis documentAnalysis);

    DocumentAnalysis run(DocumentAnalysis documentAnalysis);
}
