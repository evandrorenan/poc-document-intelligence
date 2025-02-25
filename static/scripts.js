const baseUrl = "http://localhost:8080";

const documentTypeValues = {
  TYPE1: {
    name: "Nome Tipo 1",
    jsonPath: "$.path.to.type1",
    expectedDataType: "STRING",
    promptAdditionalInfo: "Informação adicional para Tipo 1",
    actionType: "COMPARE",
    pathsToObjectKey: [{ key: "key1", value: "value1" }]
  },
  TYPE2: {
    name: "Nome Tipo 2",
    jsonPath: "$.path.to.type2",
    expectedDataType: "NUMBER",
    promptAdditionalInfo: "Informação adicional para Tipo 2",
    actionType: "OVERRIDE",
    pathsToObjectKey: [{ key: "key2", value: "value2" }]
  },
  // Add more types as needed
};

document.addEventListener("DOMContentLoaded", function () {
  generateJSONEditor({}, "editor_holder_input");

  // Field counter for unique IDs
  let fieldCounter = 0;

  function createPathToObjectKeyRow(fieldId, pathKeyCounter, key = "", value = "") {
    return `
            <div class="path-key-row" id="path-key-row-${fieldId}-${pathKeyCounter}">
                <input type="text" placeholder="Token" class="path-key" id="path-key-${fieldId}-${pathKeyCounter}" value="${key}">
                <input type="text" placeholder="Valor" class="path-value" id="path-value-${fieldId}-${pathKeyCounter}" value="${value}">
                <button type="button" class="pocbutton" onclick="removePathToObjectKeyRow('${fieldId}', '${pathKeyCounter}')" class="remove-btn">
                    Remover
                </button>
            </div>
        `;
  }

  // Function to add a new path key row
  window.addPathToObjectKeyRow = function (fieldId) {
    const pathsList = document.getElementById(`paths-list-${fieldId}`);
    const pathKeyCounter = pathsList.children.length + 1;
    const newRow = createPathToObjectKeyRow(fieldId, pathKeyCounter);
    pathsList.insertAdjacentHTML("beforeend", newRow);
  };

  window.removePathToObjectKeyRow = function (fieldId, pathKeyCounter) {
    const row = document.getElementById(
      `path-key-row-${fieldId}-${pathKeyCounter}`
    );
    if (row) {
      row.remove();
    }
  };

  // Function to create a new field
  function createField() {
    fieldCounter++;
    const fieldDiv = document.createElement("div");
    fieldDiv.className = "field-container";
    fieldDiv.id = `field-${fieldCounter}`;

    fieldDiv.innerHTML = `
            <div class="field-group">
                <label for="name-${fieldCounter}">Nome do Campo:</label>
                <input type="text" id="name-${fieldCounter}" class="field-name">

                <label for="jsonPath-${fieldCounter}">JSON Path:</label>
                <input type="text" id="jsonPath-${fieldCounter}" class="field-jsonpath">

                <div class="paths-container" id="paths-container-${fieldCounter}">
                    <label>Chaves de arrays:</label>
                    <section>
                        <div id="paths-list-${fieldCounter}">
                            ${createPathToObjectKeyRow(fieldCounter, 1)}
                        </div>
                        <button type="button" onclick="addPathToObjectKeyRow('${fieldCounter}')" class="add-btn pocbuttonq">
                            Adicionar Token
                        </button>
                    </div>
                </div>

                <label for="expectedDataType-${fieldCounter}">Tipo de Dado Esperado:</label>
                <select id="expectedDataType-${fieldCounter}" class="field-datatype">
                    <option value="STRING">String</option>
                    <option value="NUMBER">Number</option>
                    <option value="BOOLEAN">Boolean</option>
                    <option value="DATE">Date</option>
                </select>

                <label for="promptInfo-${fieldCounter}">Informações Adicionais do Prompt:</label>
                <input type="text" id="promptInfo-${fieldCounter}" class="field-promptinfo">

                <label for="actionType-${fieldCounter}">Tipo de Ação:</label>
                <select id="actionType-${fieldCounter}" class="field-actiontype">
                    <option value="COMPARE">Comparar</option>
                    <option value="OVERRIDE">Sobrepor</option>
                    <option value="LOGIC">Aplicar lógica</option>
                </select>

                <label for="documentType-${fieldCounter}">Tipo de Documento:</label>
                <select id="documentType-${fieldCounter}" class="field-documenttype">
                    <option value="TYPE1">Tipo 1</option>
                    <option value="TYPE2">Tipo 2</option>
                    <!-- Add more types as needed -->
                </select>

                <button onclick="removeField(${fieldCounter})" class="remove-field">Remover Campo</button>
            </div>
            <hr>
        `;

    document.getElementById("fieldsToCheckContainer").appendChild(fieldDiv);

    // Add event listener for document type change
    document.getElementById(`documentType-${fieldCounter}`).addEventListener("change", function () {
      const selectedType = this.value;
      const values = documentTypeValues[selectedType];

      document.getElementById(`name-${fieldCounter}`).value = values.name;
      document.getElementById(`jsonPath-${fieldCounter}`).value = values.jsonPath;
      document.getElementById(`expectedDataType-${fieldCounter}`).value = values.expectedDataType;
      document.getElementById(`promptInfo-${fieldCounter}`).value = values.promptAdditionalInfo;
      document.getElementById(`actionType-${fieldCounter}`).value = values.actionType;

      // Fill paths to object key
      const pathsList = document.getElementById(`paths-list-${fieldCounter}`);
      pathsList.innerHTML = ""; // Clear existing rows
      values.pathsToObjectKey.forEach((path, index) => {
        const newRow = createPathToObjectKeyRow(fieldCounter, index + 1, path.key, path.value);
        pathsList.insertAdjacentHTML("beforeend", newRow);
      });
    });
  }

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
    option = true;

    new JSONEditor(document.getElementById(editorHolder), {
      schema: {},
      startval: json,
      disable_array_add: option,
      disable_array_delete: option,
      disable_array_delete_all_rows: option,
      disable_array_delete_last_row: option,
      disable_array_reorder: option,
      enable_array_copy: option,
      disable_collapse: option,
      disable_edit_json: false,
      disable_properties: option,
      mode: "tree"
    });
  }

  // Add field button handler
  document
    .getElementById("addFieldButton")
    .addEventListener("click", createField);

  // document
  //   .getElementById("create-input-screen")
  //   .addEventListener("click", function () {
  //     const jsonInput = {
  //       propriedadesRurais: [
  //         {
  //           numeroMatricula: 6610,
  //           proprietarios: [
  //             { cpfCnpj: 48827495649, nome: "José Jacinto Lisboa da Silva" },
  //           ],
  //         },
  //       ],
  //     };
  //     generateJSONEditor(jsonInput, "editor_holder_input");
  //   });

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

  // Mocked requests:
  /*   async function documentAnalysisPOST() {
    let loader = `<span class="spinner"></span>`;
    document.getElementById("submitButton").innerHTML = loader;
    try {
      return new Promise((res, rej) => {
        setTimeout(() => {
          document.getElementById("submitButton").innerHTML =
            "Enviar para Análise";
          res({ protocol: "0f9d80c9-ab0f-40a4-9d55-73debaf08ce7" });
        }, 2000);
      });
    } catch (err) {
      throw new Error("error.unknown");
    }
  } */

  /*  async function documentAnalysisGET() {
    // JSON response:
    const response = `{
        "failedActions": 3,
        "succeededActions": 2,
        "outcomeType": "PARTIAL_SUCCESS",
        "outcome": "{\\"propriedadesRurais\\\":[{\\"numeroMatricula\\\":6610,\\"proprietarios\\\":[[{\\"cpfCnpj\\\":888,\\"nome\\\":\\"yyyyy\\\",\\"nomeErro\\\":\\"Informacao divergente do documento comprobatorio.\\\"}]]}]}",
        "documentExtraData": [
            "$.propriedadesRurais[?(@.numeroMatricula==6610)].area: [\\"16,2198 ha\\\"]",
            "$.propriedadesRurais[?(@.numeroMatricula==6610)].proprietarios[?(@.cpfCnpj==999)].nome: xxxxx"
        ]
    }`;

    //  Loader element:
    let loader = `<span class="spinner"></span>`;
    document.getElementById("submitButton").innerHTML = loader;

    try {
      return new Promise((res, rej) => {
        setTimeout(() => {
          document.getElementById("submitButton").innerHTML =
            "Enviar para Análise";

          res(JSON.parse(response));
        }, 2000);
      });
    } catch (err) {
      throw new Error("error.unknown");
    }
  } */

  //  Actual request:
  async function postDocumentAnalysis(body) {
    showLoader("show", "submitButton");

    try {
      const request = fetch(`${baseUrl}/api/documents/analyze`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          accept: "*/*",
        },
        body: JSON.parse(body),
      });

      const response = (await request).json();
      showLoader("hide", "submitButton");

      return response;
    } catch (err) {
      throw new Error("error.unknown");
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

  // Submit handler
  document
    .getElementById("submitButton")
    .addEventListener("click", async function () {
      const protocol = await postDocumentAnalysis({}); // I don't know what i need to send in the body, so i'm sending an empty object
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
    });
});