/*
 * dto/DepartmentAnalyticsDTO.java
 * DTO carrying aggregated salary and headcount analytics for a specific department.
 * Connects to: dto/OverallAnalyticsDTO.java, services/EmployeeService.java, controllers/EmployeeController.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import com.employee.directory.enums.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data transfer object representing department-level aggregated metrics.
 */
@Schema(description = "Department analytics metric aggregation model")
public class DepartmentAnalyticsDTO {

    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @Schema(description = "Total number of employees in department", example = "4")
    private long employeeCount;

    @Schema(description = "Average annual salary in department", example = "110750.00")
    private BigDecimal averageSalary;

    @Schema(description = "Minimum annual salary in department", example = "95000.00")
    private BigDecimal minSalary;

    @Schema(description = "Maximum annual salary in department", example = "125000.00")
    private BigDecimal maxSalary;

    @Schema(description = "Total annual payroll for department", example = "443000.00")
    private BigDecimal totalPayroll;

    @Schema(description = "Breakdown of employee counts by status")
    private Map<EmployeeStatus, Long> statusCounts;

    public DepartmentAnalyticsDTO() {
    }

    public DepartmentAnalyticsDTO(String department, long employeeCount, BigDecimal averageSalary,
                                  BigDecimal minSalary, BigDecimal maxSalary, BigDecimal totalPayroll,
                                  Map<EmployeeStatus, Long> statusCounts) {
        this.department = department;
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.totalPayroll = totalPayroll;
        this.statusCounts = statusCounts;
    }

    // Getters and Setters

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public long getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(long employeeCount) {
        this.employeeCount = employeeCount;
    }

    public BigDecimal getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(BigDecimal averageSalary) {
        this.averageSalary = averageSalary;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.maxSalary = maxSalary;
    }

    public BigDecimal getTotalPayroll() {
        return totalPayroll;
    }

    public void setTotalPayroll(BigDecimal totalPayroll) {
        this.totalPayroll = totalPayroll;
    }

    public Map<EmployeeStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<EmployeeStatus, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }
}
