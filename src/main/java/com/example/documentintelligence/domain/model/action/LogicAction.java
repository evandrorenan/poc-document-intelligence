package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogicAction extends Action {

    @Override
    public ActionType getActionType() {
        return ActionType.LOGIC;
    }

    @Override
    public ActionResult execute(DocumentAnalysis currentAnalysis) {
        return null;
    }
}