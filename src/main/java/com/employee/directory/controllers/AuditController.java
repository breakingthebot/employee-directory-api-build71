/*
 * controllers/AuditController.java
 * REST controller exposing endpoints for viewing entity audit history and system-wide change logs.
 * Connects to: services/AuditService.java, dto/AuditLogDTO.java, dto/PagedResponseDTO.java
 * Created: 2026-08-08
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.AuditLogDTO;
import com.employee.directory.dto.PagedResponseDTO;
import com.employee.directory.services.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing HTTP endpoints for accessing audit trail histories and system change logs.
 */
@RestController
@Tag(name = "Audit Trail API", description = "Operations for retrieving employee change history logs and system-wide audit records")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Retrieves historical audit trail logs for a specific employee.
     * HTTP GET /api/v1/employees/{id}/audit-history
     * 
     * @param id Employee identifier.
     * @return 200 OK with list of AuditLogDTO records.
     */
    @Operation(summary = "Get audit history for an employee", description = "Retrieves all historical creation, modification, and deletion audit records for a given employee ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit trail logs retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuditLogDTO.class))))
    })
    @GetMapping("/api/v1/employees/{id}/audit-history")
    public ResponseEntity<List<AuditLogDTO>> getEmployeeAuditHistory(
            @Parameter(description = "Employee unique ID", example = "1") @PathVariable Long id) {
        List<AuditLogDTO> history = auditService.getAuditHistoryForEmployee(id);
        return ResponseEntity.ok(history);
    }

    /**
     * Retrieves paginated system-wide audit logs.
     * HTTP GET /api/v1/audit-logs?page=0&size=10
     * 
     * @param page Zero-based page number (default: 0).
     * @param size Page size (default: 10).
     * @return 200 OK with PagedResponseDTO container.
     */
    @Operation(summary = "Get system-wide audit logs", description = "Retrieves a paginated list of all audit trail entries across the application sorted by timestamp descending.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponseDTO.class)))
    })
    @GetMapping("/api/v1/audit-logs")
    public ResponseEntity<PagedResponseDTO<AuditLogDTO>> getAllAuditLogs(
            @Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponseDTO<AuditLogDTO> response = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(response);
    }
}
