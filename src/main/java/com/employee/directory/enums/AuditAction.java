/*
 * enums/AuditAction.java
 * Enumeration representing entity mutation actions for audit logging.
 * Connects to: models/AuditLog.java
 * Created: 2026-08-08
 */
package com.employee.directory.enums;

/**
 * Action type representing entity mutations captured in audit logs.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE
}
