package com.example.documentintelligence.domain.model.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "actionType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompareAction.class, name = "COMPARE"),
        @JsonSubTypes.Type(value = LogicAction.class, name = "LOGIC"),
        @JsonSubTypes.Type(value = OverrideAction.class, name = "OVERRIDE")
})
public abstract class Action {
    public enum ActionType {
        OVERRIDE ( OverrideAction.class, 0 ),
        COMPARE  ( CompareAction.class,  1 ),
        LOGIC    ( LogicAction.class,    2 );

        private final Class<? extends Action> actionClass;
        private final int priority;

        ActionType(Class<? extends Action> actionClass, int priority) {
            this.actionClass = actionClass;
            this.priority = priority;
        }

        public Class<? extends Action> getActionClass() {
            return actionClass;
        }

        public int getPriority() {
            return priority;
        }
    }

    public Action() {
    }

    public abstract ActionType getActionType();

    public abstract ActionResult execute(List<String> referencePaths, List<String> documentPaths, String referenceData, String documentData);
}