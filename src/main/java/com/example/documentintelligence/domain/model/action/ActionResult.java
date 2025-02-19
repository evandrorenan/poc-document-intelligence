package com.example.documentintelligence.domain.model.action;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ActionResult {
    private String message;
    private String content;
}
