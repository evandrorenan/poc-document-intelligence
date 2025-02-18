package com.example.documentintelligence.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentValidationRule {
    public enum DocumentType {
        RG,
        REGISTRO_MATRICULA,
        CAFIR
    }

    private final DocumentType documentType;
    private final String modelDeploymentId;
    private final String promptAdditionalInfo;
    private final List<FieldCheckRule> fieldsToCheck;
}
