package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDocumentRepository implements DocumentRepositoryPort {
    private final Map<String, DocumentAnalysis> storage = new ConcurrentHashMap<>();

    @Override
    public void save(DocumentAnalysis analysis) {
        storage.put(analysis.getProtocol(), analysis);
    }

    @Override
    public Optional<DocumentAnalysis> findByProtocol(String protocol) {
        return Optional.ofNullable(storage.get(protocol));
    }
}
