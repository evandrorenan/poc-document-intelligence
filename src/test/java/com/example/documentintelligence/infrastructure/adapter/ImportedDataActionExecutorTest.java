package com.example.documentintelligence.infrastructure.adapter;

import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.domain.model.action.CompareAction;
import com.example.documentintelligence.domain.model.action.OverrideAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportedDataActionExecutorTest {

    @Test
    void test() throws JsonProcessingException {
        DocumentAnalysis documentAnalysis = buildInput();
        var compareAction = new CompareAction();
        var overrideAction = new OverrideAction();
        var importedDataActionExecutor = new ImportedDataActionExecutor(new ArrayList<>(List.of(compareAction, overrideAction)));

        DocumentAnalysis documentAnalysis1 = importedDataActionExecutor.analyzeDocument(documentAnalysis);
    }

    private DocumentAnalysis buildInput() throws JsonProcessingException {

        String jsonInput = """
                {
                  "protocol" : "aa13bf0f-7773-4565-a9a4-67b14ff161b5",
                  "documentValidationRule" : {
                    "documentType" : "RG",
                    "modelDeploymentId" : "document-deployment-gpt-4o",
                    "promptAdditionalInfo" : "",
                    "fieldsToCheck" : [ {
                      "name" : "Órgão emissor do documento",
                      "jsonPath" : "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='{NUMERO_DOCUMENTO}')].orgaoEmissor",
                      "pathsToObjectKey" : {
                        "{NUMERO_DOCUMENTO}" : "$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento"
                      },
                      "expectedDataType" : "STRING",
                      "promptAdditionalInfo" : "documentosIdentificação é um array. Não inclua aqui a sigla da UF do órgão emissor.",
                      "action" : {
                        "actionType" : "COMPARE"
                      }
                    }, {
                      "name" : "UF de emissão do documento",
                      "jsonPath" : "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='{NUMERO_DOCUMENTO}')].ufEmissao",
                      "pathsToObjectKey" : {
                        "{NUMERO_DOCUMENTO}" : "$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento"
                      },
                      "expectedDataType" : "STRING",
                      "promptAdditionalInfo" : "documentosIdentificação é um array",
                      "action" : {
                        "actionType" : "COMPARE"
                      }
                    } ]
                  },
                  "valid" : true,
                  "base64Document" : null,
                  "referenceData" : "{\\"codigoInstituicaoFinanceira\\":237,\\"modalidadeCredito\\":\\"CPRF\\",\\"numeroCPFCNPJ\\":\\"32157456821\\",\\"numeroPedido\\":30016065,\\"dadosCadastraisCliente\\":{\\"nomeCliente\\":\\"EVANDRO RENAN NOGUEIRA GUIMARÃES\\",\\"documentosIdentificacao\\":[{\\"numeroDocumento\\":\\"334588686\\",\\"orgaoEmissor\\":\\"PF\\",\\"ufEmissao\\":\\"MG\\"}],\\"enderecoCliente\\":[{\\"numeroSequencia\\":0,\\"numeroCEP\\":\\"05565200\\",\\"logradouro\\":\\"R EUDORO LINCOLN\\",\\"numero\\":\\"480\\",\\"complemento\\":\\"APTO 31\\",\\"bairro\\":\\"BELA VISTA\\",\\"municipio\\":\\"SAO PAULO\\",\\"uf\\":\\"SP\\"}]},\\"propriedadesRurais\\":[{\\"nomeImovel\\":\\"Fazenda 6610\\",\\"numeroMatricula\\":\\"6610\\",\\"areaTotalImovel\\":600,\\"proprietarios\\":[{\\"cpfCnpj\\":\\"48827495649\\",\\"nome\\":\\"José Jacinto\\"}]}]}",
                  "extractedData" : null,
                  "stepResults" : {
                    "azureOpenAIAnalyzer" : "{\\n  \\"dadosCadastraisCliente\\": {\\n    \\"documentosIdentificacao\\": [\\n      {\\n        \\"numeroDocumento\\": \\"334588686\\",\\n        \\"orgaoEmissor\\": \\"SECRETARIA DA SEGURANÇA PÚBLICA INSTITUTO DE IDENTIFICAÇÃO \\\\\\"RICARDO GUMBLETON DAUNT\\\\\\"\\",\\n        \\"ufEmissao\\": \\"SP\\"\\n      }\\n    ]\\n  }\\n}",
                    "azureDocumentIntelligenceAnalyzer" : "POLÍCIA\\nSÃO PAULO CIVIL SP\\nSECRETARIA DE SEGURANÇA PÚBLICA POLÍCIA CIVIL DO ESTADO DE SÃO PAULO Departamento de Inteligência da Polícia Civil - DIPOL Instituto de Identificação Ricardo Gumbleton Daunt - IIRGD\\nRG DIGITAL DO ESTADO DE SÃO PAULO\\nREPÚBLICA FEDERATIVA DO BRASIL\\nLEI Nº 7.116, DE 29 DE AGOSTO DE 1983\\nSIP\\nESTADO DE SAO PAULO\\nCPF 321574568/21\\nDNI\\nSECRETARIA DA SEGURANÇA PÚBLICA INSTITUTO DE IDENTIFICAÇÃO \\"RICARDO GUMBLETON DAUNT\\"\\nREGISTRO GERAL 33.458.868-6\\n2 VIA\\nDATA DE EXPEDIÇÃO 18/07/2019\\nREGISTRO CIVIL\\nNOME EVANDRO RENAN NOGUEIRA GUIMARAES\\nSÃO PAULO-SP BUTANTÃ CC:LV.B298/FLS°23 /Nº63890\\nFILIAÇÃO\\nJOEL FERREIRA GUIMARAES\\nT. ELEITOR\\nCTPS SÉRIE\\nPolegar Direito\\nUF\\nMARIA APARECIDA NOGUEIRA GUIMARAES\\nORGÃO EXPEDIDOR SSP-SP\\nNIS/PIS/PASEP 13073336817\\nIDENTIDADE PROFISSIONAL\\nDATA NASCIMENTO\\nFATOR RH\\n30/06/1985\\nCERT. MILITAR\\nNATURALIDADE BELO HORIZONTE - MG\\nOBSERVAÇÃO\\nCNH\\nCNS\\nde Novembre\\nNO DO BRASIL\\nREPUBLICA LICA FEDERATIVA 12 de Novembro\\nO18 1889\\nMauricio Jose Lemos Freire\\nASSINATURA DO TITULAR\\nDelegado Divisionário de Polícia IIRGD.PCSP\\nASENATURA DO DIRETOR\\nCARTEIRA DE IDENTIDADE\\nVALIDA EM TODO O TERRITORIO NACIONAL\\nOS DADOS BIOGRÁFICOS e biométricos apresentados neste documento estão contidos no RG original\\nEsse é um arquivo assinado digitalmente pela Secretaria de Segurança Pública do estado de São Paulo em conformidade com o padrão de Assinatura Digital ICP Brasil. Caso necessite acesse https://validar.iti.gov.br e faça o upload desse documento para aferir a sua conformidade. Você também pode escanear o Código QR ao lado.\\nValid\\nPOLICIA\\nCIVIL\\nGOVERNO DO ESTADO DE SÃO PAULO Secretaria da Segurança Pública",
                    "documentDataConsistencyChecker" : {
                      "REFERENCE_PATHS" : [ "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].orgaoEmissor", "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].ufEmissao" ],
                      "DOCUMENT_PATHS" : [ "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].orgaoEmissor", "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].ufEmissao" ]
                    },
                    "documentDataConsistencyCheckerMap" : {
                      "REFERENCE_PATHS" : {
                        "{\\r\\n  \\"name\\" : \\"Órgão emissor do documento\\",\\r\\n  \\"jsonPath\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='{NUMERO_DOCUMENTO}')].orgaoEmissor\\",\\r\\n  \\"pathsToObjectKey\\" : {\\r\\n    \\"{NUMERO_DOCUMENTO}\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento\\"\\r\\n  },\\r\\n  \\"expectedDataType\\" : \\"STRING\\",\\r\\n  \\"promptAdditionalInfo\\" : \\"documentosIdentificação é um array. Não inclua aqui a sigla da UF do órgão emissor.\\",\\r\\n  \\"action\\" : {\\r\\n    \\"actionType\\" : \\"COMPARE\\"\\r\\n  }\\r\\n}" : [ "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].orgaoEmissor" ],
                        "{\\r\\n  \\"name\\" : \\"UF de emissão do documento\\",\\r\\n  \\"jsonPath\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='{NUMERO_DOCUMENTO}')].ufEmissao\\",\\r\\n  \\"pathsToObjectKey\\" : {\\r\\n    \\"{NUMERO_DOCUMENTO}\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento\\"\\r\\n  },\\r\\n  \\"expectedDataType\\" : \\"STRING\\",\\r\\n  \\"promptAdditionalInfo\\" : \\"documentosIdentificação é um array\\",\\r\\n  \\"action\\" : {\\r\\n    \\"actionType\\" : \\"COMPARE\\"\\r\\n  }\\r\\n}" : [ "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].ufEmissao" ]
                      },
                      "DOCUMENT_PATHS" : {
                        "{\\r\\n  \\"name\\" : \\"Órgão emissor do documento\\",\\r\\n  \\"jsonPath\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='{NUMERO_DOCUMENTO}')].orgaoEmissor\\",\\r\\n  \\"pathsToObjectKey\\" : {\\r\\n    \\"{NUMERO_DOCUMENTO}\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento\\"\\r\\n  },\\r\\n  \\"expectedDataType\\" : \\"STRING\\",\\r\\n  \\"promptAdditionalInfo\\" : \\"documentosIdentificação é um array. Não inclua aqui a sigla da UF do órgão emissor.\\",\\r\\n  \\"action\\" : {\\r\\n    \\"actionType\\" : \\"COMPARE\\"\\r\\n  }\\r\\n}" : [ "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].orgaoEmissor" ],
                        "{\\r\\n  \\"name\\" : \\"UF de emissão do documento\\",\\r\\n  \\"jsonPath\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='{NUMERO_DOCUMENTO}')].ufEmissao\\",\\r\\n  \\"pathsToObjectKey\\" : {\\r\\n    \\"{NUMERO_DOCUMENTO}\\" : \\"$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento\\"\\r\\n  },\\r\\n  \\"expectedDataType\\" : \\"STRING\\",\\r\\n  \\"promptAdditionalInfo\\" : \\"documentosIdentificação é um array\\",\\r\\n  \\"action\\" : {\\r\\n    \\"actionType\\" : \\"COMPARE\\"\\r\\n  }\\r\\n}" : [ "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=='334588686')].ufEmissao" ]
                      }
                    }
                  },
                  "analysisDate" : null,
                  "currentState" : "PERFORM_ACTION",
                  "status" : "PENDING",
                  "errorMessage" : null
                }
                """;

        return new ObjectMapper().readValue(jsonInput, DocumentAnalysis.class);
    }

}