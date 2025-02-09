package com.example.documentintelligence.domain;

import com.example.documentintelligence.domain.model.DocumentType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the expected fields for different document types.
 * This class defines which fields should be extracted from each type of document
 * and provides methods to validate field extraction completeness.
 */
public class DocumentFields {
    private static final Map<DocumentType, List<String>> EXPECTED_FIELDS = new ConcurrentHashMap<>();

    static {
        EXPECTED_FIELDS.put(DocumentType.REGISTRO_MATRICULA, List.of(
            "NOME_PROPRIETARIO",
            "AREA_IMOVEL",
            "ENDERECO_IMOVEL",
            "BAIRRO",
            "CIDADE",
            "ESTADO",
            "DATA_MATRICULA",
            "VALOR_COMPRA"
        ));
        // Add other document types and their expected fields here
    }

    private DocumentFields() {
        // Prevent instantiation of utility class
    }

    /**
     * Gets the list of expected fields for a given document type.
     *
     * @param documentType The type of document
     * @return Unmodifiable list of expected field names
     */
    public static List<String> getExpectedFields(DocumentType documentType) {
        return Collections.unmodifiableList(EXPECTED_FIELDS.getOrDefault(documentType, List.of()));
    }

    /**
     * Checks if all expected fields for a document type are present in the extracted fields.
     *
     * @param documentType The type of document
     * @param extractedFields Map of field names to extracted values
     * @return true if all expected fields are present, false otherwise
     */
    public static boolean hasAllExpectedFields(DocumentType documentType, Map<String, String> extractedFields) {
        if (documentType == null || extractedFields == null) {
            return false;
        }
        
        List<String> expectedFields = getExpectedFields(documentType);
        return expectedFields.stream()
            .allMatch(field -> extractedFields.containsKey(field) && 
                             extractedFields.get(field) != null && 
                             !extractedFields.get(field).trim().isEmpty());
    }

    /**
     * Gets the missing fields for a document type from the extracted fields.
     *
     * @param documentType The type of document
     * @param extractedFields Map of field names to extracted values
     * @return List of field names that are expected but missing or empty
     */
    public static List<String> getMissingFields(DocumentType documentType, Map<String, String> extractedFields) {
        if (documentType == null || extractedFields == null) {
            return getExpectedFields(documentType);
        }

        return getExpectedFields(documentType).stream()
            .filter(field -> !extractedFields.containsKey(field) || 
                           extractedFields.get(field) == null || 
                           extractedFields.get(field).trim().isEmpty())
            .toList();
    }
}
