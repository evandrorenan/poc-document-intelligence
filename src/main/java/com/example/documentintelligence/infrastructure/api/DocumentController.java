package com.example.documentintelligence.infrastructure.api;

import com.example.documentintelligence.application.DocumentService;
import com.example.documentintelligence.domain.model.DocumentAnalysis;
import com.example.documentintelligence.infrastructure.api.dto.DocumentSubmissionRequest;
import com.example.documentintelligence.infrastructure.api.dto.ProtocolResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Analysis", description = "API for document validation and analysis")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/analyze")
    @Operation(summary = "Submit a document for analysis",
            description = "Submits a document in base64 format for async analysis and validation. " +
                    "Returns a protocol that can be used to check the analysis status and results.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document submitted successfully for analysis"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ProtocolResponse> submitDocument(
            @Valid @RequestBody DocumentSubmissionRequest request) {
        String protocol = documentService.submitDocument(
                request.getBase64Document(),
                request.getDocumentType()
        );
        return ResponseEntity.ok(new ProtocolResponse(protocol));
    }

    @GetMapping("/analysis/{protocol}")
    @Operation(summary = "Get analysis results",
            description = "Retrieves the analysis results for a given protocol. The analysis may be in one of three states: " +
                    "PENDING (still processing), COMPLETED (analysis finished successfully), or FAILED (analysis encountered an error).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analysis results retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Protocol not found")
    })
    public ResponseEntity<DocumentAnalysis> getAnalysisResult(
            @PathVariable String protocol) {
        DocumentAnalysis analysisResult = documentService.getAnalysisResult(protocol);
        analysisResult.setBase64Document(null);
        return ResponseEntity.ok(analysisResult);
    }
}
