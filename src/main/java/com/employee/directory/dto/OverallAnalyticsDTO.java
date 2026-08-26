/*
 * dto/OverallAnalyticsDTO.java
 * DTO carrying company-wide summary analytics and list of department metrics.
 * Connects to: dto/DepartmentAnalyticsDTO.java, services/EmployeeService.java, controllers/EmployeeController.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Top-level response container for overall analytics.
 */
@Schema(description = "Overall company-wide analytics summary model")
public class OverallAnalyticsDTO {

    @Schema(description = "Total company headcount", example = "10")
    private long totalEmployees;

    @Schema(description = "Overall company average annual salary", example = "93800.00")
    private BigDecimal overallAverageSalary;

    @Schema(description = "Total company annual payroll", example = "938000.00")
    private BigDecimal totalCompanyPayroll;

    @Schema(description = "List of department metrics breakdowns")
    private List<DepartmentAnalyticsDTO> departmentAnalytics;

    public OverallAnalyticsDTO() {
    }

    public OverallAnalyticsDTO(long totalEmployees, BigDecimal overallAverageSalary,
                               BigDecimal totalCompanyPayroll, List<DepartmentAnalyticsDTO> departmentAnalytics) {
        this.totalEmployees = totalEmployees;
        this.overallAverageSalary = overallAverageSalary;
        this.totalCompanyPayroll = totalCompanyPayroll;
        this.departmentAnalytics = departmentAnalytics;
    }

    // Getters and Setters

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public BigDecimal getOverallAverageSalary() {
        return overallAverageSalary;
    }

    public void setOverallAverageSalary(BigDecimal overallAverageSalary) {
        this.overallAverageSalary = overallAverageSalary;
    }

    public BigDecimal getTotalCompanyPayroll() {
        return totalCompanyPayroll;
    }

    public void setTotalCompanyPayroll(BigDecimal totalCompanyPayroll) {
        this.totalCompanyPayroll = totalCompanyPayroll;
    }

    public List<DepartmentAnalyticsDTO> getDepartmentAnalytics() {
        return departmentAnalytics;
    }

    public void setDepartmentAnalytics(List<DepartmentAnalyticsDTO> departmentAnalytics) {
        this.departmentAnalytics = departmentAnalytics;
    }
}
