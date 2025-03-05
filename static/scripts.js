const baseUrl = "http://localhost:8080";
// Versão 2

const documentTypeValues = {
  // Add more types as needed
  REGISTRO_MATRICULA: {
    name: "Registro de Matrícula",
    modelDeploymentId: "document-deployment-gpt-4o",
    promptAdditionalInfo: "",
    fieldsToCheck: [
      {
        name: "Nome do proprietário",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[?(@.cpfCnpj=={CPF_CNPJ})].nome",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" },
          { key: "{CPF_CNPJ}", value: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[*].cpfCnpj" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "Ignore os filtros de pesquisa. propriedadesRurais e proprietários são arrays",
        actionType: "COMPARE"
      },
      {
        name: "Área do imóvel",
        jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].area",
        pathsToObjectKey: [
          { key: "{MATRICULA}", value: "$.propriedadesRurais[*].numeroMatricula" }
        ],
        expectedDataType: "STRING",
        promptAdditionalInfo: "A unidade de medida deve ser hectares quadrados. Formato: [0-9.,]+?\\s*?ha. propriedadesRurais é um array",
        actionType: "COMPARE"
      }
    ]
  }
};

// Function to create a new field
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
        <input type="text" id="promptInfo-${fieldCounter}" class="field-promptinfo" value="${field.promptAdditionalInfo}">
        </input>

        <section class="paths-list" id="paths-list-${fieldCounter}">
        </section>

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

  // Reset fields container
  const fieldsContainer = document.getElementById("fieldsToCheckContainer");
  const existingFields = fieldsContainer.querySelectorAll(".field-container");
  existingFields.forEach(field => field.remove());

  // If REGISTRO_MATRICULA is selected, auto-populate fields
  if (selectedType === "REGISTRO_MATRICULA") {
    const typeConfig = documentTypeValues[selectedType];

    // Set model deployment ID and prompt info
    document.getElementById("modelDeploymentId").value = typeConfig.modelDeploymentId;
    // document.getElementById("promptAdditionalInfo").value = typeConfig.promptAdditionalInfo;

    // Dynamically create fields
    typeConfig.fieldsToCheck.forEach((fieldsToCheck, index) => {
      createFieldToCheck(fieldsToCheck, index + 1);
    });

    // Populate the created fields with predefined values
    typeConfig.fieldsToCheck.forEach((fieldConfig, index) => {
      const fieldId = index + 1;

      // Set basic field values
      document.getElementById(`name-${fieldId}`).value = fieldConfig.name;
      document.getElementById(`jsonPath-${fieldId}`).value = fieldConfig.jsonPath;
      document.getElementById(`expectedDataType-${fieldId}`).value = fieldConfig.expectedDataType;
      document.getElementById(`promptInfo-${fieldId}`).value = fieldConfig.promptAdditionalInfo;
      document.getElementById(`actionType-${fieldId}`).value = fieldConfig.actionType;

      // Populate paths to object key
      const pathsList = document.getElementById(`paths-list-${fieldId}`);
      pathsList.innerHTML = ""; // Clear existing rows
      fieldConfig.pathsToObjectKey.forEach((path, pathIndex) => {
        const newRow = createPathToObjectKeyRow(fieldId, pathIndex + 1, path.key, path.value);
        pathsList.insertAdjacentHTML("beforeend", newRow);
      });
    });
  }
}

document.addEventListener("DOMContentLoaded", function () {
  // Modify the existing event listener to handle document type changes
  document.getElementById("documentType").addEventListener("change", handleDocumentTypeChange);
  window.jsonEditorInput = generateJSONEditor({}, "editor_holder_input");
});


// Function to add a new path key row
function addPathToObjectKeyRow(fieldId) {
  const pathsList = document.getElementById(`paths-list-${fieldId}`);
  const pathKeyCounter = pathsList.children.length + 1;
  const newRow = createPathToObjectKeyRow(fieldId, pathKeyCounter);
  pathsList.insertAdjacentHTML("beforeend", newRow);
}

// Function to remove a path key row
function removePathToObjectKeyRow(fieldId, pathKeyCounter) {
  const row = document.getElementById(
    `path-key-row-${fieldId}-${pathKeyCounter}`
  );
  if (row) {
    row.remove();
  }
}

window.addPathToObjectKeyRow = addPathToObjectKeyRow;
window.removePathToObjectKeyRow = removePathToObjectKeyRow;

// Function to remove a field
window.removeField = function (fieldId) {
  const fieldElement = document.getElementById(`field-${fieldId}`);
  if (fieldElement) {
    fieldElement.remove();
  }
};

// Generate JSON editor:
function generateJSONEditor(json, editorHolder) {

  let option = editorHolder === "editor_holder_outputx";

  var editor = new JSONEditor(document.getElementById(editorHolder), {
    schema: {},
    startval: json,
    disable_array_add: option,
    disable_array_delete: true,
    disable_array_delete_all_rows: true,
    disable_array_delete_last_row: true,
    disable_array_reorder: true,
    enable_array_copy: false,
    disable_collapse: true,
    disable_edit_json: false,
    disable_properties: true,
    mode: "tree"
  });

  return editor;
}

// File to base64 conversion
function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result.split(",")[1]);
    reader.onerror = (error) => reject(error);
  });
}

// Status display functions
function updateStatus(message, type) {
  const statusDiv = document.getElementById("statusDisplay");
  statusDiv.textContent = message;
  statusDiv.className = `status-${type}`;
}

// Polling function
async function pollAnalysisStatus(protocol) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/documents/analysis/${protocol}`,
      {
        headers: { accept: "*/*" },
      }
    );

    const data = await response.json();

    if (response.status >= 400) {
      updateStatus(`Erro: ${data.message || "Erro na análise"}`, "error");
      return;
    }

    if (data.status === "COMPLETED") {
      updateStatus("Análise concluída com sucesso!", "success");
      return;
    }

    updateStatus("Análise em andamento...", "loading");
    setTimeout(() => pollAnalysisStatus(protocol), 2000);
  } catch (error) {
    updateStatus(`Erro: ${error.message}`, "error");
  }
}

// Function to get fields data
function getFieldsData() {
  const fields = [];
  document.querySelectorAll(".field-container").forEach((container) => {
    const fieldId = container.id.split("-")[1];
    fields.push({
      name: document.getElementById(`name-${fieldId}`).value,
      jsonPath: document.getElementById(`jsonPath-${fieldId}`).value,
      expectedDataType: document.getElementById(`expectedDataType-${fieldId}`)
        .value,
      promptAdditionalInfo: document.getElementById(`promptInfo-${fieldId}`)
        .value,
      action: {
        actionType: document.getElementById(`actionType-${fieldId}`).value,
      },
    });
  });
  return fields;
}

//  Adds a loader inbetween the button text:
function showLoader(type, id) {
  if (type === "show") {
    let loader = `<span class="spinner"></span>`;
    document.getElementById(id).innerHTML = loader;
  } else {
    document.getElementById(id).innerHTML = "Enviar para Análise";
  }
}

async function getDocumentAnalysis(protocol) {
  showLoader("show", "submitButton");

  try {
    const request = fetch(`${baseUrl}/api/documents/analysis/${protocol}`, {
      method: "GET",
      headers: {
        accept: "*/*",
      },
    });

    const response = (await request).json();
    showLoader("hide", "submitButton");

    return response;
  } catch (err) {
    throw new Error("error.unknown");
  }
}

//  Prepare the HTML element:
function getResponseElements(
  failedActions,
  succeededActions,
  outcomeType,
  documentExtraData
) {
  return `
            <div class="response-element">
                <p><b>Ações que falharam:</b> ${failedActions}</p>
                <p><b>Ações que tiveram sucesso:</b> ${succeededActions}</p>
                <p><b>Tipo de resultado:</b> ${outcomeType}</p>
                <p><b>Dados extras do documento:</b> </p>
                ${documentExtraData.map((item) => `<li>${item}</li>`)}
            </div>
        `;
}

async function postDocumentAnalysis() {
  showLoader("show", "submitButton");

  // Get reference data from JSON editor
  const referenceData = window.jsonEditorInput ?
    JSON.stringify(window.jsonEditorInput.getValue()) :
    JSON.stringify({});

  // Convert file to base64
  const fileInput = document.getElementById('base64document');
  const file = fileInput.files[0];

  if (!file) {
    throw new Error("No file selected");
  }

  const base64Document = await fileToBase64(file);

  // Collect fields to check dynamically
  const fieldsToCheck = [];
  document.querySelectorAll(".field-container").forEach((container) => {
    const fieldId = container.id.split("-")[1];

    // Collect path to object keys
    const pathsToObjectKey = {};
    container.querySelectorAll(".path-key-row").forEach((pathRow) => {
      const key = pathRow.querySelector(".path-key").value;
      const value = pathRow.querySelector(".path-value").value;
      if (key && value) {
        pathsToObjectKey[key] = value;
      }
    });

    fieldsToCheck.push({
      name: document.getElementById(`name-${fieldId}`).value,
      jsonPath: document.getElementById(`jsonPath-${fieldId}`).value,
      pathsToObjectKey: pathsToObjectKey,
      expectedDataType: document.getElementById(`expectedDataType-${fieldId}`).value,
      promptAdditionalInfo: document.getElementById(`promptInfo-${fieldId}`).value,
      action: {
        actionType: document.getElementById(`actionType-${fieldId}`).value,
        extraInfo: "" // Can be adjusted as needed
      }
    });
  });

  const requestBody = {
    documentValidationRule: {
      documentType: document.getElementById("documentType").value,
      modelDeploymentId: "document-deployment-gpt-4o", // You may want to make this configurable
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
        "accept": "*/*"
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

// Submit handler
document
  .getElementById("submitButton")
  .addEventListener("click", async function () {
    // I don't know what i need to send in the body, so i'm sending an empty object
    try {
      const protocol = await postDocumentAnalysis({});
      console.log(protocol);

      //  After getting the protocol, start polling the status:
      const response = await getDocumentAnalysis(protocol);

      document.getElementById("result").style.display = "block";
      const resultContainer = document.getElementById("resultContent");

      generateJSONEditor(JSON.parse(response.outcome), "editor_holder_output");

      resultContainer.innerHTML = getResponseElements(
        response.failedActions,
        response.succeededActions,
        response.outcomeType,
        response.documentExtraData
      );
    } catch (error) {
      console.error("Error in document analysis:", error);
      updateStatus(`Erro: ${error.message}`, "error");
    }
  });