package com.example.documentintelligence.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchParams {
    private final String pathToFieldContent;
    private final String pathsToObjectKey;
}

