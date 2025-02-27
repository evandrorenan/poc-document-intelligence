package com.example.documentintelligence.domain.model.action;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "actionType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompareAction.class, name = "COMPARE"),
        @JsonSubTypes.Type(value = LogicAction.class, name = "LOGIC"),
        @JsonSubTypes.Type(value = OverrideAction.class, name = "OVERRIDE")
})
public abstract class Action {
    public enum ActionType {COMPARE, OVERRIDE, LOGIC;}

    protected Object extraInfo;

    public Action() {
    }

    public abstract ActionType getActionType();

    public Object getExtraInfo() {
        return this.extraInfo;
    }

    public void setExtraInfo(Object extraInfo) {
        this.extraInfo = extraInfo;
    }

    public abstract ActionResult execute(DocumentAnalysis currentAnalysis);

}