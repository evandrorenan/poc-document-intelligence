package com.example.documentintelligence.infrastructure.adapter;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.*;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.port.DocumentAnalyzerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_DOCUMENT_INTELLIGENCE_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;

@Component
@Qualifier(AZURE_OPENAI_ANALYZER)
@Slf4j
public class AzureOpenAIAnalyzer implements DocumentAnalyzerPort {
    private final OpenAIClient client;
    private final String deploymentOrModelId;
    private final String azureOpenAIContext;
    private final String azureOpenAIPrompt;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public AzureOpenAIAnalyzer(OpenAIClient client,
                               @Value("${azure.openai.deployment-id}") String deploymentOrModelId,
                               @Value("${azure.openai.context}") String azureOpenAIContext,
                               @Value("${azure.openai.prompt}") String azureOpenAIPrompt) {
        this.client = client;
        this.deploymentOrModelId = deploymentOrModelId;
        this.azureOpenAIContext = azureOpenAIContext;
        this.azureOpenAIPrompt = azureOpenAIPrompt;
    }

    /*
    Avalie o seguinte conteúdo e extraia as seguintes informações específicas em formato JSON, garantindo que
     os nomes dos campos sejam interpretados como caminhos para a criação de objetos aninhados:

     %s

     Conteúdo a ser avaliado:

     %s

     Observações importantes:

     * Os nomes dos campos no JSON de retorno devem ser interpretados como caminhos para a criação de objetos aninhados,
     exceto o "$".
     Exemplo: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[?(@.seq=={SEQ})].nome", produz
     o seguinte json:
     { "propriedadesRurais": [ { "proprietarios": [ { "nome": "<texto extraído>" } ] } ] }

     * Se alguma das informações solicitadas não for encontrada no texto JSON, o campo correspondente no
     JSON de retorno deve conter o valor null.
     * O texto JSON de entrada pode conter outras informações além das solicitadas. Ignore essas informações
     adicionais.
     A resposta deve conter somente o json com as informações solicitadas. Sem nenhum comentário adicional.
     Deve iniciar com "{" e terminar com "}"
     * Não utilize nenhum tipo de formatação de campo, a não ser que seja explicitamente solicitado.
     Por exemplo, CPFs, CEPs e CNPJs não devem ter pontos ou hifens.
     */
    @Override
    public DocumentAnalysis analyzeDocument(DocumentAnalysis currentAnalysis) {
        try {

            String promptAdditionalInfo = currentAnalysis.getDocumentValidationRule().getPromptAdditionalInfo();

            String fieldList = currentAnalysis.getDocumentValidationRule().getFieldsToCheck().stream()
              .map(field -> {
                  var jsonPath = field.getJsonPath() + " //" + field.getPromptAdditionalInfo() + "\n";
                  var tokenPaths = field.getPathsToObjectKey().values().stream()
                                        .reduce((a, b) -> String.join("\n", a, b == null ? "" : b))
                                        .orElse("");
                  return String.join("\n", jsonPath, tokenPaths);
              })
              .reduce((a, b) -> String.join("\n", a, b == null ? "" : b))
              .orElse("");

            String content = String.join("\n", currentAnalysis.getStepResults().get(AZURE_DOCUMENT_INTELLIGENCE_ANALYZER).toString());

            String formattedPrompt = String.format(azureOpenAIPrompt, fieldList, content, promptAdditionalInfo);

            List<ChatRequestMessage> messages = new ArrayList<>();
            messages.add(new ChatRequestSystemMessage(azureOpenAIContext));
            messages.add(new ChatRequestUserMessage(formattedPrompt));

            ChatCompletions completions = client.getChatCompletions(
                deploymentOrModelId,
                new ChatCompletionsOptions(messages)
            );

            String response = completions.getChoices().get(0).getMessage().getContent()
                                 .replace("```json", "")
                                 .replace("```", "");
            if (!isValidJson(response)) return currentAnalysis;

            currentAnalysis.getStepResults().put(AZURE_OPENAI_ANALYZER, response);

            return currentAnalysis;

        } catch (Exception e) {
            log.error("Error processing document with OpenAI: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isValidJson(String response) {
        try {
            JsonNode root = mapper.readTree(response);
            if (root == null || root.isNull()) {
                log.error("AzureAI response is empty or null.");
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("AzureAI doesn't return a valid json as expected.");
            return false;
        }
        return true;
    }
}
