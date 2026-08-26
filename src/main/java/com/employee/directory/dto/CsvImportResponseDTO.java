/*
 * dto/CsvImportResponseDTO.java
 * DTO returning results and row-by-row error logs from bulk CSV import processing.
 * Connects to: utils/CsvHelper.java, services/EmployeeService.java, controllers/EmployeeController.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Payload returned upon completing a bulk CSV upload operation.
 */
@Schema(description = "Bulk CSV import processing summary model")
public class CsvImportResponseDTO {

    @Schema(description = "Total number of CSV rows processed", example = "25")
    private int totalRowsProcessed;

    @Schema(description = "Number of successfully created employee records", example = "23")
    private int successCount;

    @Schema(description = "Number of failed rows encountered", example = "2")
    private int failureCount;

    @Schema(description = "Detailed list of row-level validation errors", example = "[\"Row 3: Invalid email format\", \"Row 12: Email already exists\"]")
    private List<String> errors;

    @Schema(description = "List of created employee records")
    private List<EmployeeDTO> importedEmployees;

    public CsvImportResponseDTO() {
    }

    public CsvImportResponseDTO(int totalRowsProcessed, int successCount, int failureCount,
                                List<String> errors, List<EmployeeDTO> importedEmployees) {
        this.totalRowsProcessed = totalRowsProcessed;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.errors = errors;
        this.importedEmployees = importedEmployees;
    }

    // Getters and Setters

    public int getTotalRowsProcessed() {
        return totalRowsProcessed;
    }

    public void setTotalRowsProcessed(int totalRowsProcessed) {
        this.totalRowsProcessed = totalRowsProcessed;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<EmployeeDTO> getImportedEmployees() {
        return importedEmployees;
    }

    public void setImportedEmployees(List<EmployeeDTO> importedEmployees) {
        this.importedEmployees = importedEmployees;
    }
}
