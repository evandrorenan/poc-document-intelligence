package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;

public class OverrideAction extends Action {

    @Override
    public ActionType getActionType() {
        return ActionType.OVERRIDE;
    }

    @Override
    public ActionResult execute(DocumentAnalysis currentAnalysis) {
        return null;
    }
}