/*
 * enums/EmployeeEventType.java
 * Enumeration representing real-time SSE event notification types.
 * Connects to: dto/EmployeeEventDTO.java, events/EmployeeEventPublisher.java
 * Created: 2026-08-08
 */
package com.employee.directory.enums;

/**
 * Event types broadcasted over Server-Sent Events (SSE) connections.
 */
public enum EmployeeEventType {
    EMPLOYEE_CREATED,
    EMPLOYEE_UPDATED,
    EMPLOYEE_DELETED
}
