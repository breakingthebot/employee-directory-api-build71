/*
 * dto/EmployeeDTO.java
 * Data Transfer Object for creating, updating, and returning employee resources with OpenAPI Schema annotations.
 * Connects to: controllers/EmployeeController.java, services/EmployeeService.java, enums/EmployeeStatus.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import com.employee.directory.enums.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO carrying employee payload with validation constraints and OpenAPI documentation.
 */
@Schema(description = "Employee record payload model")
public class EmployeeDTO {

    @Schema(description = "Unique employee identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Employee given first name", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Schema(description = "Employee family last name", example = "Johnson", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Schema(description = "Unique employee business email address", example = "alice.johnson@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Schema(description = "Department name", example = "Engineering", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department cannot exceed 50 characters")
    private String department;

    @Schema(description = "Professional job title", example = "Senior Software Engineer", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title cannot exceed 100 characters")
    private String jobTitle;

    @Schema(description = "Annual base salary in USD", example = "125000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Salary must be non-negative")
    private BigDecimal salary;

    @Schema(description = "Date of initial employment hire", example = "2021-03-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @Schema(description = "Current employment status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    private EmployeeStatus status;

    @Schema(description = "Record creation timestamp", example = "2026-08-08T13:42:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Record last updated timestamp", example = "2026-08-08T13:42:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    public EmployeeDTO() {
    }

    public EmployeeDTO(Long id, String firstName, String lastName, String email, String department,
                       String jobTitle, BigDecimal salary, LocalDate hireDate, EmployeeStatus status,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.jobTitle = jobTitle;
        this.salary = salary;
        this.hireDate = hireDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
