package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;

public interface Action {


    public enum ActionType {COMPARE, OVERRIDE, LOGIC;}
    ActionType getActionType();

    public ActionResult execute(DocumentAnalysis currentAnalysis);

}