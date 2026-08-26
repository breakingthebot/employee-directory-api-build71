/*
 * models/AuditLog.java
 * JPA Entity capturing historical mutation records and field diffs.
 * Connects to: repositories/AuditLogRepository.java, enums/AuditAction.java
 * Created: 2026-08-08
 */
package com.employee.directory.models;

import com.employee.directory.enums.AuditAction;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an immutable audit log entry.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "change_summary", length = 2000)
    private String changeSummary;

    public AuditLog() {
    }

    public AuditLog(String entityName, Long entityId, AuditAction action, String modifiedBy, String changeSummary) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.modifiedBy = modifiedBy;
        this.timestamp = LocalDateTime.now();
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
