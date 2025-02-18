package com.example.documentintelligence.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MatchParams {
    private final String pathToFieldContent;
    private final List<FieldCheckRule> fieldCheckRules;
}

