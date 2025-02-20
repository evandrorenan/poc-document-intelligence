package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OverrideAction implements Action {

    @Override
    public ActionType getActionType() {
        return null;
    }

    @Override
    public ActionResult execute(DocumentAnalysis currentAnalysis) {
        return null;
    }
}