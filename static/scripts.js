const baseUrl = "http://localhost:8080";

const documentTypeValues = {
  REGISTRO_MATRICULA: {
    name: "Registro de Matrícula",
    modelDeploymentId: "document-deployment-gpt-4o",
    promptAdditionalInfo: "",
    fieldsToCheck: [
      {
        name: "Número da Matrícula",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].numeroMatricula",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "PropriedadesRurais é um array",
        actionType: "COMPARE"
      },
      {
        name: "Área total do imóvel",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].areaTotalImovel",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "A unidade de medida deve ser hectares quadrados. Formato: [0-9.,]+?\\s*?ha. propriedadesRurais é um array",
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
        promptAdditionalInfo: "O CEP possui 8 caracteres numéricos. Não utilize formatações.",
        actionType: "COMPARE"
      },
      {
        name: "Logradouro do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.logradouro",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "",
        actionType: "COMPARE"
      },
      {
        name: "Numero do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.numero",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "Nem todas as propriedades rurais possuem número. Se não encontrar, retorne conteúdo vazio.",
        actionType: "COMPARE"
      },
      {
        name: "Complemento do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.complemento",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "Nem todas as propriedades rurais possuem complemento. Se não encontrar, retorne conteúdo vazio.",
        actionType: "COMPARE"
      },
      {
        name: "Município do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.municipio",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "",
        actionType: "COMPARE"
      },
      {
        name: "UF do endereço",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].endereco.uf",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "",
        actionType: "COMPARE"
      }
    ]
  },

};

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
    <hr>
  `;

  document.getElementById("fieldsToCheckContainer").appendChild(fieldDiv);
}

function handleDocumentTypeChange() {
  const selectedType = this.value;

  const fieldsContainer = document.getElementById("fieldsToCheckContainer");
  const existingFields = fieldsContainer.querySelectorAll(".field-container");
  existingFields.forEach(field => field.remove());

  if (selectedType === "REGISTRO_MATRICULA") {
    const typeConfig = documentTypeValues[selectedType];

    document.getElementById("modelDeploymentId").textContent = typeConfig.modelDeploymentId;

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
    disable_collapse: true,
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
      return;
    }

    if (data.status === "COMPLETED") {
      updateStatus("Análise concluída com sucesso!", "success");
      if (!data.extractedData) {
        updateStatus("Nenhum dado extra extraído", "error");
        return;
      }

      document.getElementById("editor_holder_output").innerHTML = "";
      generateJSONEditor(JSON.parse(data.extractedData.outcome), "editor_holder_output");

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
  showLoader("show", "submitButton");

  try {
    const request = await fetch(`${baseUrl}/api/documents/analysis/${protocol}`, {
      method: "GET",
      headers: { accept: "*/*" }
    });

    const response = await request.json();
    showLoader("hide", "submitButton");

    return response;
  } catch (err) {
    showLoader("hide", "submitButton");
    updateStatus(`Erro: ${err.message}`, "error");
    throw err;
  }
}

function getResponseElements(failedActions, succeededActions, outcomeType, documentExtraData) {
  return `
    <div class="response-element">
        <p><b>Ações que falharam:</b> ${failedActions}</p>
        <p><b>Ações que tiveram sucesso:</b> ${succeededActions}</p>
        <p><b>Tipo de resultado:</b> ${outcomeType}</p>
        <p><b>Dados extras do documento:</b> </p>
        ${documentExtraData.map((item) => `<li>${item}</li>`).join('')}
    </div>
  `;
}

async function postDocumentAnalysis() {
  showLoader("show", "submitButton");

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
    showLoader("hide", "submitButton");

    return protocol;
  } catch (err) {
    showLoader("hide", "submitButton");
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

    document.getElementById("result").style.display = "block";
    const resultContainer = document.getElementById("resultContent");

    // generateJSONEditor(JSON.parse(response.outcome), "editor_holder_output");

    resultContainer.innerHTML = getResponseElements(
      response.failedActions,
      response.succeededActions,
      response.outcomeType,
      response.documentExtraData.replace(": ", ":\n")
    );
  } catch (error) {
    console.error("Error in document analysis:", error);
    updateStatus(`Erro: ${error.message}`, "error");
  }
});