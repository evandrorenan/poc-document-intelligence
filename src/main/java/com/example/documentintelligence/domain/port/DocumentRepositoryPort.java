package com.example.documentintelligence.domain.port;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import java.util.Optional;

public interface DocumentRepositoryPort {
    void save(DocumentAnalysis analysis);
    Optional<DocumentAnalysis> findByProtocol(String protocol);
}
