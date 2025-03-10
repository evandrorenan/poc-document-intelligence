package com.example.documentintelligence.domain.model.action;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.*;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.FieldCheckRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.documentintelligence.domain.model.action.ActionResult.ActionOutcomeType.FAILURE;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;

@Slf4j
@Component
public class LogicAction extends Action {

    private final OpenAIClient client;
    private final String deploymentOrModelId;
    private final String azureOpenAIContext;

    @Autowired
    public LogicAction(OpenAIClient client,
                       @Value("${azure.openai.deployment-id}") String deploymentOrModelId,
                       @Value("${azure.openai.context}") String azureOpenAIContext) {
        this.client = client;
        this.deploymentOrModelId = deploymentOrModelId;
        this.azureOpenAIContext = azureOpenAIContext;
    }



    /*
 Matrícula precisa ter certificação negativa de ônus. Como identificar? (valid = true/false)
 Selo de registro. (valid = true/false)
 Cpf Cnpj do proprietario COMPARE
 Cidade COMPARE
 Area COMPARE
 Unidade de medida COMPARE
 Comarca COMPARE
 Nome dos proprietários COMPARE
 Data emissao LOGIC
 Tipo de imóvel
 UF



  */
    @Override
    public ActionType getActionType() {
        return ActionType.LOGIC;
    }

    @Override
    public ActionResult execute(List<String> referencePaths, List<String> documentPaths, String referenceData, String documentData) {
        return null;
    }

    public ActionResult execute(DocumentAnalysis documentAnalysis) {

        ActionResult actionResult = new ActionResult();
        actionResult.setDocumentExtraData(new ArrayList<>());

        Map<String, Map<FieldCheckRule, List<String>>> validationResults =
                getValidationResults(documentAnalysis);

        if (validationResults.isEmpty()) {
            return handleEmptyValidationResults(actionResult);
        }

        String documentData = (String) documentAnalysis.getStepResults().getOrDefault(AZURE_OPENAI_ANALYZER, "");

//        validationResults.keySet().stream().map(field -> {
//
//        });
        //JsonPathProcessor.readPathAsList(documentData, );

        List<ChatRequestMessage> messages = new ArrayList<>();
        messages.add(new ChatRequestSystemMessage(azureOpenAIContext));
        messages.add(new ChatRequestUserMessage("""
                
                """));

        ChatCompletions completions = client.getChatCompletions(
                deploymentOrModelId,
                new ChatCompletionsOptions(messages)
        );

        String response = completions.getChoices().get(0).getMessage().getContent()
                                     .replace("```json", "")
                                     .replace("```", "");


        return actionResult;
    }

    private Map<String, Map<FieldCheckRule, List<String>>> getValidationResults(DocumentAnalysis documentAnalysis) {
        var validationResults = documentAnalysis.getStepResults().getOrDefault(
                VALIDATE_FIELD_CONTENT_ANALYZER + "Map", Collections.emptyMap());

        if (!(validationResults instanceof Map<?, ?>)) {
            log.warn("Validation step didn't produce a valid Map");
            return Collections.emptyMap();
        }

        return (Map<String, Map<FieldCheckRule, List<String>>>) validationResults;
    }

    private ActionResult handleEmptyValidationResults(ActionResult actionResult) {
        log.warn("Validation step produced invalid results");
        actionResult.setOutcomeType(FAILURE);
        return actionResult;
    }
}