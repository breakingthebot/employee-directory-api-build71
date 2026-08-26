/*
 * dto/EmployeeEventDTO.java
 * DTO representing a real-time event notification payload.
 * Connects to: enums/EmployeeEventType.java, events/EmployeeEventPublisher.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import com.employee.directory.enums.EmployeeEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Event model broadcasted over SSE streams upon directory mutations.
 */
@Schema(description = "Real-time SSE event notification model")
public class EmployeeEventDTO {

    @Schema(description = "Event classification type", example = "EMPLOYEE_CREATED")
    private EmployeeEventType eventType;

    @Schema(description = "Event occurrence timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Target employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Human-readable event summary", example = "New employee record created: Alice Johnson")
    private String summary;

    @Schema(description = "Associated employee record payload")
    private EmployeeDTO payload;

    public EmployeeEventDTO() {
    }

    public EmployeeEventDTO(EmployeeEventType eventType, Long employeeId, String summary, EmployeeDTO payload) {
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.employeeId = employeeId;
        this.summary = summary;
        this.payload = payload;
    }

    // Getters and Setters

    public EmployeeEventType getEventType() {
        return eventType;
    }

    public void setEventType(EmployeeEventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public EmployeeDTO getPayload() {
        return payload;
    }

    public void setPayload(EmployeeDTO payload) {
        this.payload = payload;
    }
}
