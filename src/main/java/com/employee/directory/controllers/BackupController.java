/*
 * controllers/BackupController.java
 * REST controller managing database backup snapshot creation, listing, and restoration.
 * Connects to: services/BackupService.java
 * Created: 2026-08-08
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.ApiErrorResponse;
import com.employee.directory.services.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller providing HTTP endpoints for triggering database backup snapshots and listing backup files.
 */
@RestController
@RequestMapping("/api/v1/backups")
@Tag(name = "Disaster Recovery & Backups API", description = "Operations for triggering database backup snapshots, listing backup files, and disaster recovery")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    /**
     * Triggers an immediate database backup snapshot creation.
     * HTTP POST /api/v1/backups/create
     * 
     * @return 201 CREATED with generated snapshot details.
     */
    @Operation(summary = "Create database backup snapshot", description = "Generates an immediate JSON database backup snapshot file containing all employee records and audit logs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Backup snapshot created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Administrative privileges required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createBackup() {
        String filename = backupService.createDatabaseBackupSnapshot();
        return new ResponseEntity<>(Map.of("message", "Backup snapshot created successfully", "filename", filename), HttpStatus.CREATED);
    }

    /**
     * Lists available backup snapshot files.
     * HTTP GET /api/v1/backups
     * 
     * @return 200 OK with list of snapshot filenames.
     */
    @Operation(summary = "List backup snapshots", description = "Retrieves a list of all generated database backup snapshot filenames.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of backup files retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Administrative privileges required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<String>> listBackups() {
        List<String> backups = backupService.listBackupSnapshots();
        return ResponseEntity.ok(backups);
    }

    /**
     * Restores database state from a specified snapshot file.
     * HTTP POST /api/v1/backups/restore
     * 
     * @param filename Target snapshot filename.
     * @return 200 OK with status message.
     */
    @Operation(summary = "Restore database snapshot", description = "Restores database state from a specified backup snapshot file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Database state restored successfully"),
            @ApiResponse(responseCode = "400", description = "Snapshot file not found or invalid format",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Administrative privileges required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/restore")
    public ResponseEntity<Map<String, String>> restoreBackup(
            @Parameter(description = "Backup snapshot filename to restore", example = "snapshot_20260808_145000.json", required = true)
            @RequestParam String filename) {

        boolean success = backupService.restoreBackupSnapshot(filename);
        return ResponseEntity.ok(Map.of("message", "Database restoration completed successfully", "filename", filename));
    }
}
