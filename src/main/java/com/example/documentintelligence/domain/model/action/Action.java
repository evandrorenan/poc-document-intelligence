package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.FieldCheckRule;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

@Data
@Slf4j
public abstract class Action {
    public abstract ActionResult execute(DocumentAnalysis currentAnalysis);

    public enum ActionType {COMPARE, OVERRIDE, LOGIC}

    private final ActionType actionType;
    private final Map<String, Object> actionParams;

    ActionResult execute(FieldCheckRule fieldCheckRule) {
        throw new NotImplementedException("Not implemented yet");
    }
}