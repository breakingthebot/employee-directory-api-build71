/*
 * dto/AuditLogDTO.java
 * DTO carrying audit record data for REST responses.
 * Connects to: controllers/AuditController.java, services/AuditService.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import com.employee.directory.enums.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Payload model representing an audit log entry.
 */
@Schema(description = "Audit trail change record model")
public class AuditLogDTO {

    @Schema(description = "Audit log unique ID", example = "1")
    private Long id;

    @Schema(description = "Target entity name", example = "Employee")
    private String entityName;

    @Schema(description = "Target entity unique ID", example = "1")
    private Long entityId;

    @Schema(description = "Mutation action type", example = "UPDATE")
    private AuditAction action;

    @Schema(description = "Username of the user executing the change", example = "admin")
    private String modifiedBy;

    @Schema(description = "Timestamp of the mutation event")
    private LocalDateTime timestamp;

    @Schema(description = "Summary of field changes and before/after values", example = "lastName: Johnson -> Johnson-Smith; salary: 125000.00 -> 145000.00")
    private String changeSummary;

    public AuditLogDTO() {
    }

    public AuditLogDTO(Long id, String entityName, Long entityId, AuditAction action,
                       String modifiedBy, LocalDateTime timestamp, String changeSummary) {
        this.id = id;
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.modifiedBy = modifiedBy;
        this.timestamp = timestamp;
        this.changeSummary = changeSummary;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
}
