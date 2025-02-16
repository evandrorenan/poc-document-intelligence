package com.example.documentintelligence.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Builder
@Getter
public class FieldCheckRule {

    private String name;
    private String friendlyName;
    private String jsonPath;
    private ExpectedDataType expectedDataType;
    private String promptAdditionalInfo;

    @AllArgsConstructor
    public enum ExpectedDataType {
        STRING(String.class),
        INTEGER(Integer.class),
        DOUBLE(Double.class),
        BOOLEAN(Boolean.class);

        private final Class<?> clazz;
    }
}
