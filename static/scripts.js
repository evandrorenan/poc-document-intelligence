document.addEventListener('DOMContentLoaded', function () {
    // Field counter for unique IDs
    let fieldCounter = 0;

    function createPathToObjectKeyRow(fieldId, pathKeyCounter) {
        return `
            <div class="path-key-row" id="path-key-row-${fieldId}-${pathKeyCounter}">
                <input type="text" placeholder="Chave" class="path-key" id="path-key-${fieldId}-${pathKeyCounter}">
                <input type="text" placeholder="Valor" class="path-value" id="path-value-${fieldId}-${pathKeyCounter}">
                <button type="button" onclick="removePathToObjectKeyRow('${fieldId}', '${pathKeyCounter}')" class="remove-btn">
                    Remover
                </button>
            </div>
        `;
    }

    window.removePathToObjectKeyRow = function(fieldId, pathKeyCounter) {
        const row = document.getElementById(`path-key-row-${fieldId}-${pathKeyCounter}`);
        if (row) {
            row.remove();
        }
    };

    // Function to create a new field
    function createField() {
        fieldCounter++;
        const fieldDiv = document.createElement('div');
        fieldDiv.className = 'field-container';
        fieldDiv.id = `field-${fieldCounter}`;

        fieldDiv.innerHTML = `
            <div class="field-group">
                <label for="name-${fieldCounter}">Nome do Campo:</label>
                <input type="text" id="name-${fieldCounter}" class="field-name">

                <label for="jsonPath-${fieldCounter}">JSON Path:</label>
                <input type="text" id="jsonPath-${fieldCounter}" class="field-jsonpath">

                <div class="paths-container" id="paths-container-${fieldCounter}">
                    <label>Paths to Object Key:</label>
                    <div id="paths-list-${fieldCounter}">
                        ${createPathToObjectKeyRow(fieldCounter, 1)}
                    </div>
                    <button type="button" onclick="addPathToObjectKeyRow('${fieldCounter}')" class="add-btn">
                        Adicionar Path Key
                    </button>
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
                    <option value="COMPARE">Compare</option>
                    <option value="EXTRACT">Extract</option>
                    <option value="VALIDATE">Validate</option>
                </select>

                <button onclick="removeField(${fieldCounter})" class="remove-field">Remover Campo</button>
            </div>
            <hr>
        `;

        document.getElementById('fieldsToCheckContainer').appendChild(fieldDiv);
    }

    // Function to remove a field
    window.removeField = function(fieldId) {
        const fieldElement = document.getElementById(`field-${fieldId}`);
        if (fieldElement) {
            fieldElement.remove();
        }
    };

    // Add field button handler
    document.getElementById('addFieldButton').addEventListener('click', createField);

    // Existing JSON Editor initialization
    document.getElementById('create-input-screen').addEventListener('click', function () {
        const jsonInput = `{
            "documentValidationRule": {
                "documentType": "REGISTRO_MATRICULA",
                "modelDeploymentId": "document-deployment-gpt-4o",
                "promptAdditionalInfo": "",
                "fieldsToCheck": [
                    {
                        "name": "Nome do proprietário",
                        "jsonPath": "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[?(@.cpfCnpj=={CPF_CNPJ})].nome",
                        "pathsToObjectKey": {
                            "{MATRICULA}": "$.propriedadesRurais[*].numeroMatricula",
                            "{CPF_CNPJ}": "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[*].cpfCnpj"
                        },
                        "expectedDataType": "STRING",
                        "promptAdditionalInfo": "Ignore os filtros de pesquisa. propriedadesRurais e proprietários são arrays",
                        "action": {
                            "actionType": "COMPARE"
                        }
                    },
                    {
                        "name": "Área do imóvel",
                        "jsonPath": "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].area",
                        "pathsToObjectKey": {
                            "{MATRICULA}": "$.propriedadesRurais[*].numeroMatricula"
                        },
                        "expectedDataType": "STRING",
                        "promptAdditionalInfo": "A unidade de medida deve ser hectares quadrados. Formato: [0-9.,]+?\\\\s*?ha. propriedadesRurais é um array",
                        "action": {
                            "actionType": "COMPARE"
                        }
                    }
                ]
            },
            "referenceData": "{ \\"propriedadesRurais\\\": [ { \\"numeroMatricula\\\": 6610, \\"proprietarios\\\": [ { \\"cpfCnpj\\\": 48827495649, \\"nome\\\": \\"José Jacinto Lisboa da Silva\\\" } ] } ] }",
            "base64Document": null
        }`;
        let parsedJson;

        try {
            parsedJson = JSON.parse(jsonInput);
        } catch (e) {
            alert('Invalid JSON format. Please check your input.');
            return;
        }

        const editor = new JSONEditor(document.getElementById('editor_holder'), {
            schema: parsedJson,
            disable_collapse: false,
            disable_edit_json: false,
            disable_one_click: false,
            disable_properties: false,
            mode: 'tree'
        });
    });

    // File to base64 conversion
    function fileToBase64(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result.split(',')[1]);
            reader.onerror = error => reject(error);
        });
    }

    // Status display functions
    function updateStatus(message, type) {
        const statusDiv = document.getElementById('statusDisplay');
        statusDiv.textContent = message;
        statusDiv.className = `status-${type}`;
    }

    // Polling function
    async function pollAnalysisStatus(protocol) {
        try {
            const response = await fetch(`http://localhost:8080/api/documents/analysis/${protocol}`, {
                headers: { 'accept': '*/*' }
            });

            const data = await response.json();

            if (response.status >= 400) {
                updateStatus(`Erro: ${data.message || 'Erro na análise'}`, 'error');
                return;
            }

            if (data.status === 'COMPLETED') {
                updateStatus('Análise concluída com sucesso!', 'success');
                return;
            }

            updateStatus('Análise em andamento...', 'loading');
            setTimeout(() => pollAnalysisStatus(protocol), 2000);
        } catch (error) {
            updateStatus(`Erro: ${error.message}`, 'error');
        }
    }

    // Function to get fields data
    function getFieldsData() {
        const fields = [];
        document.querySelectorAll('.field-container').forEach(container => {
            const fieldId = container.id.split('-')[1];
            fields.push({
                name: document.getElementById(`name-${fieldId}`).value,
                jsonPath: document.getElementById(`jsonPath-${fieldId}`).value,
                expectedDataType: document.getElementById(`expectedDataType-${fieldId}`).value,
                promptAdditionalInfo: document.getElementById(`promptInfo-${fieldId}`).value,
                action: {
                    actionType: document.getElementById(`actionType-${fieldId}`).value
                }
            });
        });
        return fields;
    }

    // Submit handler
    document.getElementById('submitButton').addEventListener('click', async function() {
        try {
            const fileInput = document.getElementById('base64document');
            const file = fileInput.files[0];
            if (!file) {
                throw new Error('Por favor, selecione um documento');
            }

            updateStatus('Processando documento...', 'loading');
            const base64Document = await fileToBase64(file);

            const fieldsToCheck = getFieldsData();

            const payload = {
                documentValidationRule: {
                    documentType: document.getElementById('documentType').value,
                    modelDeploymentId: document.getElementById('modelDeploymentId').value,
                    promptAdditionalInfo: document.getElementById('promptAdditionalInfo').value,
                    fieldsToCheck: fieldsToCheck.length > 0 ? fieldsToCheck : [
                        {
                            name: "Nome do proprietário",
                            jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[?(@.cpfCnpj=={CPF_CNPJ})].nome",
                            pathsToObjectKey: {
                                "{MATRICULA}": "$.propriedadesRurais[*].numeroMatricula",
                                "{CPF_CNPJ}": "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].proprietarios[*].cpfCnpj"
                            },
                            expectedDataType: "STRING",
                            promptAdditionalInfo: "Ignore os filtros de pesquisa. propriedadesRurais e proprietários são arrays",
                            action: {
                                actionType: "COMPARE"
                            }
                        },
                        {
                            name: "Área do imóvel",
                            jsonPath: "$.propriedadesRurais[?(@.numeroMatricula=={MATRICULA})].area",
                            pathsToObjectKey: {
                                "{MATRICULA}": "$.propriedadesRurais[*].numeroMatricula"
                            },
                            expectedDataType: "STRING",
                            promptAdditionalInfo: "A unidade de medida deve ser hectares quadrados. Formato: [0-9.,]+?\\s*?ha. propriedadesRurais é um array",
                            action: {
                                actionType: "COMPARE"
                            }
                        }
                    ]
                },
                referenceData: JSON.stringify(editor.getValue()),
                base64Document: base64Document
            };

            const response = await fetch('http://localhost:8080/api/documents/analyze', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'accept': '*/*'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (response.ok) {
                updateStatus('Iniciando análise...', 'loading');
                pollAnalysisStatus(data.protocol);
            } else {
                throw new Error(data.message || 'Erro ao enviar documento');
            }
        } catch (error) {
            updateStatus(`Erro: ${error.message}`, 'error');
        }
    });
});