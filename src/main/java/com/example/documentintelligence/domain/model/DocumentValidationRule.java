package com.example.documentintelligence.domain.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = DocumentValidationRule.DocumentValidationRuleBuilder.class)
public class DocumentValidationRule {
    public enum DocumentType {
        RG,
        REGISTRO_MATRICULA,
        COMPROVANTE_DE_ENDERECO
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class DocumentValidationRuleBuilder {}

    private final DocumentType documentType;
    private final String modelDeploymentId;
    private final String promptAdditionalInfo;
    private final List<FieldCheckRule> fieldsToCheck;
}
