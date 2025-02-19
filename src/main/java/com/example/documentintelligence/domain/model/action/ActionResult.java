package com.example.documentintelligence.domain.model.action;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ActionResult {
    public enum ActionOutcomeType { SUCCESS, PARTIAL_SUCCESS, FAILURE, UNCHANGED }

    private int failedActions;
    private int succeededActions;
    private ActionOutcomeType outcomeType;
    private String outcome;
}
