package com.example.documentintelligence.domain.model;

import com.example.documentintelligence.domain.model.action.Action;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Builder
@Getter
@Jacksonized
public class FieldCheckRule {

    private String name;
    private String jsonPath;
    private Map<String, String> pathsToObjectKey;
    private ExpectedDataType expectedDataType;
    private String promptAdditionalInfo;
    private Action action;

    @AllArgsConstructor
    public enum ExpectedDataType {
        STRING(String.class),
        INTEGER(Integer.class),
        DOUBLE(Double.class),
        BOOLEAN(Boolean.class);

        private final Class<?> clazz;
    }
}