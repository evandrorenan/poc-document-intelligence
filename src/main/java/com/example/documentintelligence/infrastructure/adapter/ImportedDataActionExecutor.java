package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.action.Action;
import com.example.documentintelligence.domain.model.action.CompareAction;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.*;

@Component
@Qualifier(IMPORTED_DATA_ACTION_EXECUTOR)
public class ImportedDataActionExecutor implements DocumentAnalyzerPort {

    private final List<Action> actions;

    @Autowired
    public ImportedDataActionExecutor(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {

        Action action = currentAnalysis.getDocumentValidationRule().getFieldsToCheck().get(0).getAction();
        switch (action.getActionType()) {
            case COMPARE -> {
                new CompareAction().getActionType();

            }
            case OVERRIDE -> {
            }
            case LOGIC -> {
            }
        }


        return currentAnalysis;
    }
}
