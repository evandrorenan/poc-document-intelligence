package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.DocumentValidationRule;
import com.example.documentintelligence.domain.model.FieldCheckRule;
import com.example.documentintelligence.domain.model.action.Action;
import com.example.documentintelligence.domain.model.action.CompareAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.documentintelligence.domain.model.FieldCheckRule.ExpectedDataType.STRING;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.AZURE_OPENAI_ANALYZER;
import static com.example.documentintelligence.domain.workflow.AnalyzerQualifiers.VALIDATE_FIELD_CONTENT_ANALYZER;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.DOCUMENT_PATHS;
import static com.example.documentintelligence.infrastructure.adapter.DocumentDataConsistencyChecker.REFERENCE_PATHS;

@Slf4j
public class DocumentDataConsistencyCheckerTest {

    @Test
    void shouldValidateComplexArrayField() {
        // Given
        List<FieldCheckRule> fieldsToCheck = getFieldCheckRules();
        String referenceData = loadReferenceData();
        String documentData = loadDocumentData();

        DocumentAnalysis documentAnalysis = createDocumentAnalysis(referenceData, documentData, fieldsToCheck);

        // When
        DocumentAnalysis result = new DocumentDataConsistencyChecker().analyzeDocument(documentAnalysis);

        // Then
        Assertions.assertDoesNotThrow(() -> {
            Map<String, List<String>> pathsMap = (Map<String, List<String>>) result.getStepResults().get(VALIDATE_FIELD_CONTENT_ANALYZER);
            List<String> referencePaths = pathsMap.get(REFERENCE_PATHS);
            referencePaths.forEach(path -> JsonPath.parse(referenceData).read(path));
            List<String> documentPaths = pathsMap.get(DOCUMENT_PATHS);
            documentPaths.forEach(path -> JsonPath.parse(referenceData).read(path));
        });
    }

    @Test
    void test() throws JsonProcessingException {

        var mapper = new ObjectMapper();
        String referenceData = "{ \"propriedadesRurais\": [ { \"numeroMatricula\": 10, \"proprietarios\": [ { \"seq\": 11, \"nome\": \"Austregesilo\" } ] } ] }";

        String stepResults = """
                {"azureOpenAIAnalyzer":"\\n{\\n  \\"$\\": {\\n    \\"propriedadesRurais\\": [\\n      {\\n        \\"proprietarios\\": [\\n          {\\n            \\"nome\\": \\"José Jacinto Lisboa\\"\\n          },\\n          {\\n            \\"nome\\": \\"Adelaide Maria Lisboa\\"\\n          }\\n        ]\\n      }\\n    ]\\n  }\\n}\\n","azureDocumentIntelligenceAnalyzer":"REPÚBLICA FEDERATIVA DO BRASIL\\nCOMARCA DE PIRANGA\\nCIDADE DE PIRANGA\\nESTADO DE MINAS GERAIS\\nCARTORIO DO REGISTRO DE IMÓVEIS Marcelo Somda Licio. Oficial\\nCertifico, a requerimento verbal de pessoa interessada, que revendo em cartório os Livros de\\nRegistro Geral, deles no Livro 02, vê-se a matrícula do seguinte teor: Mat.6610 - Data: 24 de janeiro de 2013. Imóvel rural, nominalmente assim identificado: 16,2198ha de terras, no lugar denominado Catas Altas, município de Presidente Bernardes-MG, INCRA 436 160 006 700, ITR 06366392, (códigos estes de uma área maior) com as seguintes divisas: Inicia-se a descrição deste perímetro no vértice PT-V-31, de coordenadas N 7.702.870,0748m e E 695.322,2108m; Cerca; deste, segue confrontando com ALBERTO LISBOA, com os seguintes azimutes e distâncias: 151°14'50\\" e 291,164 m até o vértice PT-V-39, de coordenadas N 7.702.614,8101m e E 695.462,2691m; Estrada e cerca; deste, segue confrontando com ALBERTO LISBOA, com os seguintes azimutes e distâncias: 234°28'32\\" e 46,958 m até o vértice PT-V-40, de coordenadas N 7.702.587,5249m e E 695.424,0513m; 233°08'45\\" e 50,821 m até o vértice PT-V-41, de coordenadas N 7.702.557,0437m e E 695.383,3864m; 240°36'56\\" e 43,427 m até o vértice PT-V-42, de coordenadas N 7.702.535,7354m e E 695.345,5463m; 250°39'29\\" e 42,817 m até o vértice PT-V-43, de coordenadas N 7.702.521,5543m e E 695.305,1462m; 262°49'51\\" e 47,592 m até o vértice PT-V-44, de coordenadas N 7.702.515,6150m e E 695.257,9261m; 263º12'36\\" e 36,970 m até o vértice PT-V-45, de coordenadas N 7.702.511,2439m e E 695.221,2153m; 249°56'43\\" e 62,582 m até o vértice PT-V-46, de coordenadas N 7.702.489,7834m e E 695.162,4275m; 245°27'46\\" e 75,469 m até o vértice PT-V-47, de coordenadas N 7.702.458,4425m e E 695.093,7742m; 244°25'02\\" e 42,606 m até o vértice PT-V-48, de coordenadas N 7.702.440,0445m e E 695.055,3449m; 259º32'58\\" e 52,119 m até o vértice PT-V-49, de coordenadas N 7.702.430,5908m e E 695.004,0909m; 254°12'37\\" e 42,430 m até o vértice PT-V-50, de coordenadas N 7.702.419,0453m e E 694.963,2618m; 240°52'22\\" e 33,963 m até o vértice PT-V-51, de coordenadas N 7.702.402,5139m e E 694.933,5938m; 198°12'43\\" e 5,773 m até o vértice PT-V-24, de coordenadas N 7.702.397,0303m e E 694.931,7896m; Cerca; deste, segue confrontando com OTACILIO SOARES COUTO VIDIGAL, com os seguintes azimutes e distâncias: 317º53'39\\" e 147,258 m até o vértice PT-V-25, de coordenadas N 7.702.506,2821m e E 694.833,0528m; Rio; deste, segue confrontando com RIO PIRANGA, com os seguintes azimutes e distâncias: 21°59'25\\" e 135,175 m até o vértice PT-V-26, de coordenadas N 7.702.631,6230m e E 694.883,6689m; 22°22'20\\" e 120,827 m até o vértice PT-V-27, de coordenadas N 7.702.743,3555m e E 694.929,6586m; 51º29'45\\" e 55,484 m até o vértice PT-V-28, de coordenadas N 7.702.777,8981m e E 694.973,0778m; 103°53'55\\" e 124,534 m até o vértice PT-V-29, de coordenadas N 7.702.747,9843m e E 695.093,9660m; 78°11'16\\" e 112,716 m até o vértice PT-V-30, de coordenadas N 7.702.771,0580m e E 695.204,2951m; 49°58'44\\" e 153,975 m até o vértice PT-V-31, ponto inicial da descrição deste perímetro. Todas as coordenadas aqui descritas estão georreferenciadas ao Sistema Geodésico Brasileiro, e encontram-se representadas no Sistema U T M, referenciadas ao Meridiano Central nº 45°00', fuso -23K, tendo como datum o SIRGAS 2000. Todos os azimutes e distâncias, área e perímetro foram calculados no plano de projeção U T M. Proprietário: José Jacinto Lisboa, brasileiro, casado sob o regime de\\ncomunhão parcial de bens com Adelaide Maria Lisboa, autônomo, CPF 488.274.956-49. ela CPF 958.469.566-53, residentes na Travessa Américo Curi Carneiro, nº 33, Visconde do Rio Branco-MG. Nº do registro anterior: Livro 2, mat.3470, Av.16 e Av.17. Oficial(a). Marcelo Tomaz Lúcio.\\nR.01-6610, prot. 27.266, livro 1-M de 24/02/2023. Emitente: José Jacinto Lisboa, brasileiro. CPF 488.274.956-49, RG M-2.717.267 SSP/MG, residente na Travessa Américo Curi Carneiro, nº33, Visconde do Rio Branco-MG, casado sob regime de comunhão universal de bens, e Adelaide Maria Lisboa, brasileira, CPF 958.469.566-53, CI M-5.398.127. diretor de empresa, residente no mesmo endereço. Garantidores: José Jacinto Lisboa, brasileiro. CPF 488.274.956-49, RG M-2.717.267 SSP/MG, residente na Travessa Américo Curi Carneiro. nº33, Visconde do Rio Branco-MG, casado sob regime de comunhão universal de bens. e Adelaide Maria Lisboa, brasileira, CPF 958.469.566-53, CI M-5.398.127, diretor de empresa. residente no mesmo endereço. Credor: Banco Bradesco S/A, CGC 60.746.948/0001-12. endereço Núcleo Cidade de Deus, n/n, Vila Yara, CEP 060.29-900, Osasco/SP, agencia 2527- 5. Título de Transmissão ou de Ônus: Cédula de Credito Bancário CCB 441594. Forma do Título, sua Procedência e Caracterização: os emitentes dão em garantia de hipoteca cedular de primeiro grau sem concorrência de terceiros o imóvel rural Sitio Catas Altas. no município de Presidente Bernardes, desta comarca de Piranga- MG, com 16,2100ha de terras. valor do imóvel R$325.000,00, em razão de financiamento de oitenta e dois mil reais . Destinação -modalidade Custeio agrícola lavoura de milho não se aplica irrigadas. Período / Safra 2022/2023, quantidade de área 12,0000. Unidade de medida -hectare. Produção estimada- quantidade 98,28 unidade medida- tonelada. Encargos- taxa de juros contratada- 14,5000% ao ano 1,135 % ao mês IOF 0,380000% sobre o valor liberado. Taxa de Juros Boleto14,5000% ao ano 1,135% ao mês Taxa de Juros - Débito em conta- 14,50000% ao ano 1,135% ao mês. Forma de pagamento- 28/09/2023, Valor R$82.000,00. Meio de Pagamento- débito em conta. Ordem de Precedência do débito- número de ordem 01, Agência02527-5. conta corrente 000000001664-0. Data de emissão - Visconde de Rio Branco, 23 de fevereiro de 2023. Vencimento : 28/09/2023. Praça de Pagamento: Visconde do Rio Branco-MG. Tudo de conformidade com as cláusulas e condições inserida na cédula a qual uma via fica arquivada em cartório. Piranga, 24 de fevereiro de 2023. Oficial, Marcelo Tomaz Lúcio. Certifica mais que o imóvel encontra-se com ônus de hipoteca e livre e desembaraçado de ações pessoais reipersecutórias, até a presente data. O referido é verdade do que dou fé. Eu. Oficial que digitei, subscrevo e assino.\\nPiranga, 29 de maio de 2023.\\nPODER JUDICIÁRIO - TJMG CORREGEDORIA GERAL DE JUSTIÇA Cartório de Reg. de Imóveis de Piranga - MG\\nSelo Eletrônico Nº GHW85055 Cód. Seg .: 9313.8585.1268.5509\\nPedido Certidão Nº 23/858 - criado em 29/05/2023 Quantidade de Atos Praticados: 003 - data: 29/05/2023\\nEmol .: R$ 118,81 + TFJ: R$ 27,99 = Valor Final: R$ 146,80\\nConsulte a validade deste Selo no site:"}
                """;
        Map<String, Object> mapStepResults = mapper.readValue(stepResults, new TypeReference<Map<String, Object>>() {
        });

         Map<String, String> x = new ObjectMapper().readValue("{ \"{MATRICULA}\": \"$.propriedadesRurais[*].numeroMatricula\", \"{SEQ}\": \"$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[*].seq\" }", HashMap.class);

        var y= FieldCheckRule.builder()
                .name("Nome do proprietário")
                .jsonPath("$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[?(@.seq=={SEQ})].nome")
                .pathsToObjectKey(new ObjectMapper().readValue("{ \"{MATRICULA}\": \"$.propriedadesRurais[*].numeroMatricula\", \"{SEQ}\": \"$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[*].seq\" }", java.util.HashMap.class))
                .expectedDataType(STRING)
                .promptAdditionalInfo("")
                .action(new CompareAction())
                .build();

        String s = new ObjectMapper().writeValueAsString(y);
        new ObjectMapper().readValue(s, FieldCheckRule.class);






        List<FieldCheckRule> fieldsToCheck = new ObjectMapper().readValue(
                "[ { \"name\": \"Nome do proprietário\", \"jsonPath\": \"$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[?(@.seq=={SEQ})].nome\", \"pathsToObjectKey\": { \"{MATRICULA}\": \"$.propriedadesRurais[*].numeroMatricula\", \"{SEQ}\": \"$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[*].seq\" }, \"expectedDataType\": \"STRING\", \"promptAdditionalInfo\": \"Ignore os filtros de pesquisa. propriedadesRurais e proprietários são arrays\", \"action\": { \"actionType\": \"COMPARE\" } }, { \"name\": \"Área do imóvel\", \"jsonPath\": \"$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].area\", \"pathsToObjectKey\": { \"{MATRICULA}\": \"$.propriedadesRurais[*].numeroMatricula\" }, \"expectedDataType\": \"STRING\", \"promptAdditionalInfo\": \"A unidade de medida deve ser hectares quadrados. Formato: [0-9.,]+?\\\\s*?ha. propriedadesRurais é um array\", \"action\": { \"actionType\": \"COMPARE\" } } ] }",
                new TypeReference<List<FieldCheckRule>>() {
                });

        DocumentValidationRule documentValidationRule =
                DocumentValidationRule.builder()
                                      .documentType(DocumentValidationRule.DocumentType.REGISTRO_MATRICULA)
                                      .modelDeploymentId("document-deployment-gpt-4o")
                                      .promptAdditionalInfo("")
                                      .fieldsToCheck(fieldsToCheck)
                                      .build();

        DocumentAnalysis documentAnalysis = DocumentAnalysis.builder()
                                                            .referenceData(referenceData)
                                                            .documentValidationRule(documentValidationRule)
                                                            .stepResults(mapStepResults)
                                                            .build();

        DocumentAnalysis result = new DocumentDataConsistencyChecker().analyzeDocument(documentAnalysis);

        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    private DocumentAnalysis createDocumentAnalysis(
            String referenceData,
            String documentData,
            List<FieldCheckRule> fieldCheckRules) {

        Map<String, Object> stepResults = new HashMap<>();
        stepResults.put(AZURE_OPENAI_ANALYZER, documentData);
        var documentValidationRule =
                DocumentValidationRule.builder()
                                      .fieldsToCheck(fieldCheckRules)
                                      .build();

        return DocumentAnalysis.builder()
                               .referenceData(referenceData)
                               .stepResults(stepResults)
                               .documentValidationRule(documentValidationRule)
                               .build();
    }

    public static List<FieldCheckRule> getFieldCheckRules() {

        Map<String, String> pathsToObjectKeyMap = new LinkedHashMap<>(); //to preserver order
        pathsToObjectKeyMap.put("${MATRICULA}", "$.propriedadesRurais[*].numeroMatricula");
        pathsToObjectKeyMap.put("${SEQ}", "$.propriedadesRurais[?(@.numeroMatricula==${MATRICULA})].proprietarios[*].seq");

        Action action = null;

        FieldCheckRule baseFieldCheckRule = FieldCheckRule.builder()
                                                          .name("nome")
                                                          .jsonPath("$.propriedadesRurais[?(@.numeroMatricula==${MATRICULA})].proprietarios[?(@.seq==${SEQ})].nome")
                                                          .pathsToObjectKey(pathsToObjectKeyMap)
                                                          .expectedDataType(STRING)
                                                          .action(action)
                                                          .promptAdditionalInfo("Do not format. Content regex: [0-9a-zA-Z]{14}|[0-9a-zA-Z]{11}")
                                                          .build();

        return List.of(baseFieldCheckRule);
    }

    public static String loadDocumentData() {
        return """
                    {
                      "cpfCnpj": "321",
                      "propriedadesRurais": [
                        {
                          "proprietarios": [
                            {"seq": 0, "nome": "Claudia"},
                            {"seq": 1, "nome": "José"},
                            {"seq": 2, "nome": "Maria"},
                            {"seq": 3, "nome": "Eva"}
                          ],
                          "numeroMatricula": 123
                        }
                      ]
                    }
                """;
    }

    public static String loadReferenceData() {
        return """
                    {
                      "propriedadesRurais": [
                        {
                          "proprietarios": [
                            {"seq": 0, "nome": "Maria"},
                            {"seq": 1, "nome": "José"},
                            {"seq": 3, "nome": "Benedita"}
                          ],
                          "numeroMatricula": 123
                        }
                      ],
                      "cpfCnpj": "321"
                    }
                """;
    }

}