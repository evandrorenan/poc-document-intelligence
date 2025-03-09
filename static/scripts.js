const baseUrl = "http://localhost:8080";

const exampleDocument = {
    "codigoInstituicaoFinanceira": 237,
    "modalidadeCredito": "CPRF",
    "numeroCPFCNPJ": "32157456821",
    "numeroPedido": 30016065,
    "dadosCadastraisCliente": {
        "nomeCliente": "EVANDRO RENAN NOGUEIRA GUIMARÃES",
        "documentosIdentificacao": [
            {
                "numeroDocumento": "334588686",
                "orgaoEmissor": "SSP",
                "ufEmissao": "MG"
            }
        ],
        "enderecoCliente": [
            {
                "numeroSequencia": 0,
                "numeroCEP": "05565200",
                "logradouro": "R EUDORO LINCOLN",
                "numero": "480",
                "complemento": "APTO 31",
                "bairro": "BELA VISTA",
                "municipio": "SAO PAULO",
                "uf": "SP"
            }
        ]
    },
    "propriedadesRurais": [
        {
            "nomeImovel": "Fazenda 6610",
            "numeroMatricula": "6610",
            "areaTotalImovel": 600,
            "proprietarios": [
                {
                    "cpfCnpj": "48827495649",
                    "nome": "José Jacinto"
                }
            ]
        }
    ]
}

const documentTypeValues = {
  RG: {
    name: "RG",
    modelDeploymentId: "document-deployment-gpt-4o",
    promptAdditionalInfo: "",
    fieldsToCheck: [
      {
        name: "Órgão emissor do documento",
        jsonPath: "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=={NUMERO_DOCUMENTO})].orgaoEmissor",
        pathsToObjectKey: [
          { key: "{NUMERO_DOCUMENTO}", value: "$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "documentosIdentificação é um array. Não inclua aqui a sigla da UF do órgão emissor.",
        actionType: "COMPARE"
      },
      {
        name: "UF de emissão do documento",
        jsonPath: "$.dadosCadastraisCliente.documentosIdentificacao[?(@.numeroDocumento=={NUMERO_DOCUMENTO})].ufEmissao",
        pathsToObjectKey: [
          { key: "{NUMERO_DOCUMENTO}", value: "$.dadosCadastraisCliente.documentosIdentificacao[*].numeroDocumento" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "documentosIdentificação é um array",
        actionType: "COMPARE"
      },
    ]
  },
  COMPROVANTE_DE_ENDERECO: {
    name: "Comprovante de endereço",
    modelDeploymentId: "document-deployment-gpt-4o",
    promptAdditionalInfo: "",
    fieldsToCheck: [
      {
        name: "Logradouro do endereço",
        jsonPath: "$.dadosCadastraisCliente.enderecoCliente[?(@.numeroCEP==\"{NUMERO_CEP}\")].logradouro",
        pathsToObjectKey: [
          { key: "{NUMERO_CEP}", value: "$.dadosCadastraisCliente.enderecoCliente[*].numeroCEP" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "enderecoCliente é um array",
        actionType: "COMPARE"
      },
      {
        name: "Número do endereço",
        jsonPath: "$.dadosCadastraisCliente.enderecoCliente[?(@.numeroCEP==\"{NUMERO_CEP}\")].numero",
        pathsToObjectKey: [
          { key: "{NUMERO_CEP}", value: "$.dadosCadastraisCliente.enderecoCliente[*].numeroCEP" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "enderecoCliente é um array. Se o número do imóvel não estiver explícito, deixe este campo vazio. Não inclua aqui dados complementares como bloco, torre, andar, sala, casa, fundos, etc",
        actionType: "COMPARE"
      },
      {
        name: "Complemento do endereço",
        jsonPath: "$.dadosCadastraisCliente.enderecoCliente[?(@.numeroCEP==\"{NUMERO_CEP}\")].complemento",
        pathsToObjectKey: [
          { key: "{NUMERO_CEP}", value: "$.dadosCadastraisCliente.enderecoCliente[*].numeroCEP" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "enderecoCliente é um array. Se o complemento do imóvel não estiver explícito, deixe este campo vazio. Inclua aqui dados complementares como bloco, torre, andar, sala, casa, fundos, etc",
        actionType: "COMPARE"
      },
      {
        name: "Bairro do endereço",
        jsonPath: "$.dadosCadastraisCliente.enderecoCliente[?(@.numeroCEP==\"{NUMERO_CEP}\")].bairro",
        pathsToObjectKey: [
          { key: "{NUMERO_CEP}", value: "$.dadosCadastraisCliente.enderecoCliente[*].numeroCEP" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "enderecoCliente é um array",
        actionType: "COMPARE"
      },
      {
        name: "Município do endereço",
        jsonPath: "$.dadosCadastraisCliente.enderecoCliente[?(@.numeroCEP==\"{NUMERO_CEP}\")].municipio",
        pathsToObjectKey: [
          { key: "{NUMERO_CEP}", value: "$.dadosCadastraisCliente.enderecoCliente[*].numeroCEP" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "enderecoCliente é um array",
        actionType: "COMPARE"
      },
      {
        name: "UF do endereço",
        jsonPath: "$.dadosCadastraisCliente.enderecoCliente[?(@.numeroCEP==\"{NUMERO_CEP}\")].uf",
        pathsToObjectKey: [
          { key: "{NUMERO_CEP}", value: "$.dadosCadastraisCliente.enderecoCliente[*].numeroCEP" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "enderecoCliente é um array",
        actionType: "COMPARE"
      }
    ]
  },
  REGISTRO_MATRICULA: {
    name: "Registro de Matrícula",
    modelDeploymentId: "document-deployment-gpt-4o",
    promptAdditionalInfo: `
    O conteúdo pode mencionar antigos proprietários responsáveis pela venda dos proprietários atuais, ignore-os e retorne somente os proprietários atuais.\nDados de retorno devem respeitar o schema abaixo: \n
{ \"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": { \"codigoInstituicaoFinanceira\": { \"type\": \"integer\" }, \"modalidadeCredito\": { \"type\": \"string\" }, \"numeroCPFCNPJ\": { \"type\": \"string\", \"pattern\": \"^\\\\d{11,14}$\" }, \"numeroPedido\": { \"type\": \"integer\" }, \"dadosCadastraisCliente\": { \"type\": \"object\", \"properties\": { \"nomeCliente\": { \"type\": \"string\" }, \"documentosIdentificacao\": { \"type\": \"array\", \"items\": { \"type\": \"object\", \"properties\": { \"numeroDocumento\": { \"type\": \"string\" }, \"orgaoEmissor\": { \"type\": \"string\" }, \"ufEmissao\": { \"type\": \"string\", \"pattern\": \"^[A-Z]{2}$\" } } } }, \"enderecoCliente\": { \"type\": \"array\", \"items\": { \"type\": \"object\", \"properties\": { \"numeroSequencia\": { \"type\": \"integer\" }, \"numeroCEP\": { \"type\": \"string\", \"pattern\": \"^\\\\d{8}$\" }, \"logradouro\": { \"type\": \"string\" }, \"numero\": { \"type\": \"string\" }, \"complemento\": { \"type\": \"string\" }, \"bairro\": { \"type\": \"string\" }, \"municipio\": { \"type\": \"string\" }, \"uf\": { \"type\": \"string\", \"pattern\": \"^[A-Z]{2}$\" } } } } }, \"required\": [\"nomeCliente\", \"documentosIdentificacao\", \"enderecoCliente\"] }, \"propriedadesRurais\": { \"type\": \"array\", \"items\": { \"type\": \"object\", \"properties\": { \"nomeImovel\": { \"type\": \"string\" }, \"numeroMatricula\": { \"type\": \"string\" }, \"areaTotalImovel\": { \"type\": \"string\" }, \"proprietarios\": { \"type\": \"array\", \"items\": { \"type\": \"object\", \"properties\": { \"cpfCnpj\": { \"type\": \"string\", \"pattern\": \"^\\\\d{11,14}$\" }, \"nome\": { \"type\": \"string\" } }, \"required\": [\"cpfCnpj\", \"nome\"] } } } } } }}
    `,
    fieldsToCheck: [
      {
        name: "Nome do proprietário",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[?(@.cpfCnpj=={CPF_CNPJ})].nome",
        pathsToObjectKey: [
            { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" },
            { key: "{CPF_CNPJ}", value: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[*].cpfCnpj" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "Ignore os filtros de pesquisa. propriedadesRurais e proprietários são arrays. O texto pode mencionar o cônjuge do proprietário somente para registro, ignore-os. Retorne o nome do cônjuge somente se ele for mencionado especificamente como proprietário.",
        actionType: "COMPARE"        
      },
      {
        name: "Número da Matrícula",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].numeroMatricula",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "PropriedadesRurais é um array.",
        actionType: "COMPARE"
      },
      {
        name: "Área total do imóvel",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].areaTotalImovel",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "A unidade de medida deve ser hectares quadrados. Formato: [0-9.,]+?\\s{1}ha. propriedadesRurais é um array.",
        actionType: "COMPARE"
      },
      {
        name: "Nome do imóvel",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].nomeImovel",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "Algumas propriedades rurais podem ter seu nome declarado na matrícula. Caso não encontre, retorne esse campo com valor vazio. propriedadesRurais é um array",
        actionType: "COMPARE"
      },
      {
        name: "Cep do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.numeroCEP",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "O CEP possui 8 caracteres numéricos. Não utilize formatações. Não confunda o endereço de pessoas listadas no documento com o endereço do imóvel de registro. Se essa informação não for encontrada, não inclua esse dado no json de retorno.",
        actionType: "COMPARE"
      },
      {
        name: "Logradouro do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.logradouro",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "endereco é um array.  Se essa informação não for encontrada, não inclua esse dado no json de retorno - não inclua no json de retorno, nem mesmo com conteúdo null. Não confunda o endereço de pessoas listadas no documento com o endereço do imóvel de registro. Se essa informação não for encontrada, não inclua esse dado no json de retorno.",
        actionType: "COMPARE"
      },
      {
        name: "Numero do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.numero",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "endereco é um array.  Se essa informação não for encontrada, não inclua esse dado no json de retorno - não inclua no json de retorno, nem mesmo com conteúdo null. Não confunda o endereço de pessoas listadas no documento com o endereço do imóvel de registro. Se essa informação não for encontrada, não inclua esse dado no json de retorno. Não inclua aqui dados complementares como bloco, torre, andar, sala, casa, fundos, etc.",
        actionType: "COMPARE"
      },
      {
        name: "Complemento do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.complemento",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "endereco é um array. Não confunda o endereço de pessoas listadas no documento com o endereço do imóvel de registro. Se essa informação não for encontrada, não inclua esse dado no json de retorno - não inclua no json de retorno, nem mesmo com conteúdo null. Inclua aqui dados complementares como bloco, torre, andar, sala, casa, fundos, etc.",
        actionType: "COMPARE"
      },
      {
        name: "Município do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.municipio",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "endereco é um array. Não confunda o endereço de pessoas listadas no documento com o endereço do imóvel de registro. Se essa informação não for encontrada, não inclua esse dado no json de retorno - não inclua no json de retorno, nem mesmo com conteúdo null.",
        actionType: "COMPARE"
      },
      {
        name: "UF do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.uf",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "endereco é um array. Não confunda o endereço de pessoas listadas no documento com o endereço do imóvel de registro. Se essa informação não for encontrada, não inclua esse dado no json de retorno - não inclua no json de retorno, nem mesmo com conteúdo null.",
        actionType: "COMPARE"
      }
    ]
  },

};


function loadExample() {
  window.jsonEditorInput.setValue(exampleDocument);
}

function switchOutputInputJson() {
  const outputJson = window.jsonEditorOutput.getValue();
  window.jsonEditorInput.setValue(outputJson);
}

function toggleAccordionBody(element) {
  const fieldCounter = element.getAttribute("data-field-index");
  const accordionBody = document.getElementById(`content-${fieldCounter}`);
  const accordionHead = document.getElementById(`accordion-head-${fieldCounter}`);
  
  if (!accordionBody) return;

  if (accordionBody.style.display === "block" || accordionBody.style.display === "") {
    accordionBody.style.display = "none";
    accordionHead.textContent = "+ " + document.getElementById("name-" + fieldCounter).value;
  } else {
    accordionBody.style.display = "block";
    accordionHead.textContent = "- ";
  }
}

function createFieldToCheck(field, fieldCounter) {
  const fieldDiv = document.createElement("div");
  fieldDiv.className = "field-container";
  fieldDiv.id = `field-${fieldCounter}`;

  const pathsToObjectKey = field.pathsToObjectKey.map((path, index) => {
    return `
      <div class="path-key-row" id="path-key-row-${fieldCounter}-${index + 1}" data-field-id="${fieldCounter}" data-key-counter="${index + 1}">
        <input type="text" class="path-key" placeholder="Token" value="${path.key}">
        <input type="text" class="path-value" placeholder="Path do token" value="${path.value}">
      </div>`;
  });

  fieldDiv.innerHTML = `
    <div class="field-group">
      <div class="accordionHead" id="accordion-head-${fieldCounter}" onclick="toggleAccordionBody(this)" data-field-index="${fieldCounter}" />
        - 
      </div>

      <div id="content-${fieldCounter}" class="accordionBody">

        <label for="name-${fieldCounter}" class="label-bold">Nome do Campo:</label>
        <input type="text" id="name-${fieldCounter}" class="field-name" value="${field.name}"></input>

        <label for="jsonPath-${fieldCounter}">JSON Path:</label>
        <input type="text" id="jsonPath-${fieldCounter}" class="field-jsonpath" value="${field.jsonPath}"></input>

        <div class="paths-container" id="paths-container-${fieldCounter}">
            <label>Tokens:</label>
            ${pathsToObjectKey.join("")}
        </div>

        <label for="expectedDataType-${fieldCounter}">Tipo de Dado Esperado:</label>
        <select id="expectedDataType-${fieldCounter}" class="field-datatype" value="${field.expectedDataType}">
            <option value="STRING">String</option>
            <option value="NUMBER">Number</option>
            <option value="BOOLEAN">Boolean</option>
            <option value="DATE">Date</option>
        </select>

        <label for="promptInfo-${fieldCounter}">Informações adicionais para o Prompt:</label>
        <input type="text" id="promptInfo-${fieldCounter}" class="field-promptinfo" value="${field.promptAdditionalInfo}"></input>

        <section class="paths-list" id="paths-list-${fieldCounter}"></section>

        <label for="actionType-${fieldCounter}">Tipo de Ação:</label>
        <select id="actionType-${fieldCounter}" class="field-actiontype" value="${field.actionType}">
            <option value="COMPARE">Comparar</option>
            <option value="OVERRIDE">Sobrepor</option>
            <option value="LOGIC">Aplicar lógica</option>
        </select>
      </div>
    </div>
  `;

  document.getElementById("fieldsToCheckContainer").appendChild(fieldDiv);
}

function handleDocumentTypeChange() {
  const selectedType = this.value;

  const fieldsContainer = document.getElementById("fieldsToCheckContainer");
  const existingFields = fieldsContainer.querySelectorAll(".field-container");
  existingFields.forEach(field => field.remove());

  if (selectedType != "") {
    const typeConfig = documentTypeValues[selectedType];

    document.getElementById("modelDeploymentId").value = typeConfig.modelDeploymentId;
    document.getElementById("promptAdditionalInfo").textContent = typeConfig.promptAdditionalInfo;

    typeConfig.fieldsToCheck.forEach((fieldsToCheck, index) => {
      createFieldToCheck(fieldsToCheck, index + 1);
    });

    typeConfig.fieldsToCheck.forEach((fieldConfig, index) => {
      const fieldId = index + 1;

      document.getElementById(`name-${fieldId}`).value = fieldConfig.name;
      document.getElementById(`jsonPath-${fieldId}`).value = fieldConfig.jsonPath;
      document.getElementById(`expectedDataType-${fieldId}`).value = fieldConfig.expectedDataType;
      document.getElementById(`promptInfo-${fieldId}`).value = fieldConfig.promptAdditionalInfo;
      document.getElementById(`actionType-${fieldId}`).value = fieldConfig.actionType;

      const pathsList = document.getElementById(`paths-list-${fieldId}`);
      pathsList.innerHTML = "";
      // fieldConfig.pathsToObjectKey.forEach((path, pathIndex) => {
        // const newRow = createPathToObjectKeyRow(fieldId, pathIndex + 1, path.key, path.value);
        // pathsList.insertAdjacentHTML("beforeend", newRow);
      // });
    });
  }
}

document.addEventListener("DOMContentLoaded", function () {
  document.getElementById("documentType").addEventListener("change", handleDocumentTypeChange);
  window.jsonEditorInput = generateJSONEditor({}, "editor_holder_input");
});

function addPathToObjectKeyRow(fieldId) {
  const pathsList = document.getElementById(`paths-list-${fieldId}`);
  const pathKeyCounter = pathsList.children.length + 1;
  const newRow = createPathToObjectKeyRow(fieldId, pathKeyCounter);
  pathsList.insertAdjacentHTML("beforeend", newRow);
}

function removePathToObjectKeyRow(fieldId, pathKeyCounter) {
  const row = document.getElementById(`path-key-row-${fieldId}-${pathKeyCounter}`);
  if (row) {
    row.remove();
  }
}

window.addPathToObjectKeyRow = addPathToObjectKeyRow;
window.removePathToObjectKeyRow = removePathToObjectKeyRow;

window.removeField = function (fieldId) {
  const fieldElement = document.getElementById(`field-${fieldId}`);
  if (fieldElement) {
    fieldElement.remove();
  }
};

function generateJSONEditor(json, editorHolder) {
  const option = editorHolder === "editor_holder_output";

  return new JSONEditor(document.getElementById(editorHolder), {
    schema: {},
    startval: json,
    disable_array_add: true,
    disable_array_delete: true,
    disable_array_delete_all_rows: true,
    disable_array_delete_last_row: true,
    disable_array_reorder: true,
    enable_array_copy: true,
    disable_collapse: false,
    disable_edit_json: false,
    disable_properties: true,
    mode: "tree"
  });
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result.split(",")[1]);
    reader.onerror = (error) => reject(error);
  });
}

function updateStatus(message, type) {
  const statusDiv = document.getElementById("statusDisplay");
  statusDiv.textContent = message;
  statusDiv.className = `status-${type}`;
}

async function pollAnalysisStatus(protocol) {
  try {
    const response = await fetch(`${baseUrl}/api/documents/analysis/${protocol}`, {
      headers: { accept: "*/*" }
    });

    const data = await response.json();

    if (response.status >= 400) {
      updateStatus(`Erro: ${data.message || "Erro na análise"}`, "error");
      showLoader("hide", "submitButton");
      document.getElementById("resultSection").style.display = "block";
      document.getElementById("editor_holder_output").innerHTML = "";
  
      return;
    }

    if (data.status !== "PENDING") {
      updateStatus("Análise concluída com sucesso!", "success");
      showLoader("hide", "submitButton");
      document.getElementById("resultSection").style.display = "block";

      if (!data.extractedData) {
        updateStatus("Nenhum dado extra extraído", "error");
        document.getElementById("resultSection").style.display = "block";
        document.getElementById("editor_holder_output").innerHTML = "";
        return;
      }

      document.getElementById("editor_holder_output").innerHTML = "";
      window.jsonEditorOutput = generateJSONEditor(JSON.parse(data.extractedData.outcome), "editor_holder_output");

      document.getElementById("resultContent").innerHTML = getResponseElements(
        data.extractedData.failedActions,
        data.extractedData.succeededActions,
        data.extractedData.outcomeType,
        data.extractedData.documentExtraData
      );
      return;
    }

    updateStatus("Análise em andamento...", "loading");
    setTimeout(() => pollAnalysisStatus(protocol), 2000);
  } catch (error) {
    updateStatus(`Erro: ${error.message}`, "error");
  }
}

function getFieldsData() {
  const fields = [];
  document.querySelectorAll(".field-container").forEach((container) => {
    const fieldId = container.id.split("-")[1];
    fields.push({
      name: document.getElementById(`name-${fieldId}`).value,
      jsonPath: document.getElementById(`jsonPath-${fieldId}`).value,
      expectedDataType: document.getElementById(`expectedDataType-${fieldId}`).value,
      promptAdditionalInfo: document.getElementById(`promptInfo-${fieldId}`).value,
      pathsToObjectKey: Object.assign({}, ...

        Array.from(document.getElementById(`paths-container-${fieldId}`).children)
          .filter(child => child.tagName.toLowerCase() != 'label')
          .map((pathRow) => {
            const key = pathRow.querySelector(".path-key").value;
            const value = pathRow.querySelector(".path-value").value;
            return { [key]: value };
          })
      ),
      action: {
        actionType: document.getElementById(`actionType-${fieldId}`).value
      }
    });
  });
  return fields;
}

function showLoader(type, id) {
  if (type === "show") {
    const loader = `<span class="spinner"></span>`;
    document.getElementById(id).innerHTML = loader;
  } else {
    document.getElementById(id).innerHTML = "Enviar para Análise";
  }
}

async function getDocumentAnalysis(protocol) {

  try {
    const request = await fetch(`${baseUrl}/api/documents/analysis/${protocol}`, {
      method: "GET",
      headers: { accept: "*/*" }
    });

    const response = await request.json();

    return response;
  } catch (err) {
    updateStatus(`Erro: ${err.message}`, "error");
    throw err;
  }
}

function getResponseElements(failedActions, succeededActions, outcomeType, documentExtraData) {
  return `
    <div class="response-element">
        <p><b>Tipo de resultado:</b> ${outcomeType}</p>
        <p><b>Dados extras do documento:</b> </p>
        ${documentExtraData.map((item) => `<li>${item}</li>`).join('')}
        <p><b>Dados de referência atualizados:</b> </p>
        <span class="je-header je-object__title">
            <button type="button" class="json-editor-btn-collapse json-editor-btntype-toggle" onclick="switchOutputInputJson()">
                <span>Usar no próximo input</span>
            </button>
        </span>
    </div>
  `;
}

async function postDocumentAnalysis() {
  showLoader("show", "submitButton");
  document.getElementById("resultSection").style.display = "none";

  const referenceData = window.jsonEditorInput ? JSON.stringify(window.jsonEditorInput.getValue()) : JSON.stringify({});

  const fileInput = document.getElementById('base64document');
  const file = fileInput.files[0];

  if (!file) {
    throw new Error("No file selected");
  }

  const base64Document = await fileToBase64(file);

  const fieldsToCheck = getFieldsData();

  const requestBody = {
    documentValidationRule: {
      documentType: document.getElementById("documentType").value,
      modelDeploymentId: "document-deployment-gpt-4o",
      promptAdditionalInfo: document.getElementById("promptAdditionalInfo").value,
      fieldsToCheck: fieldsToCheck
    },
    referenceData: referenceData,
    base64Document: base64Document
  };

  try {
    const response = await fetch(`${baseUrl}/api/documents/analyze`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        accept: "*/*"
      },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const responseJson = await response.json();
    const protocol = responseJson.protocol;

    return protocol;
  } catch (err) {
    showLoader("hide", "submitButton");
    document.getElementById("resultSection").style.display = "block";
    document.getElementById("editor_holder_output").innerHTML = "";
  
    updateStatus(`Erro: ${err.message}`, "error");
    throw err;
  }
}

document.getElementById("submitButton").addEventListener("click", async function (event) {
  event.preventDefault();
  try {
    const protocol = await postDocumentAnalysis();
    console.log(protocol);

    const response = await getDocumentAnalysis(protocol);

    pollAnalysisStatus(protocol);

  } catch (error) {
    console.error("Error in document analysis:", error);
    updateStatus(`Erro: ${error.message}`, "error");
  }
});