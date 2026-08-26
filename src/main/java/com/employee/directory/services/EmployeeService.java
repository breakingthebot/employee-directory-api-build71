/*
 * services/EmployeeService.java
 * Service interface declaring core business logic contracts for employee management, analytics, and CSV import/export.
 * Connects to: dto/EmployeeDTO.java, dto/PagedResponseDTO.java, dto/OverallAnalyticsDTO.java, dto/CsvImportResponseDTO.java, controllers/EmployeeController.java
 * Created: 2026-08-08
 */
package com.employee.directory.services;

import com.employee.directory.dto.CsvImportResponseDTO;
import com.employee.directory.dto.EmployeeDTO;
import com.employee.directory.dto.OverallAnalyticsDTO;
import com.employee.directory.dto.PagedResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.Writer;

/**
 * Service interface for business operations on Employee entities.
 */
public interface EmployeeService {

    /**
     * Creates a new employee record.
     * 
     * @param dto Data transfer object containing employee details.
     * @return Created EmployeeDTO containing generated ID and timestamps.
     */
    EmployeeDTO createEmployee(EmployeeDTO dto);

    /**
     * Retrieves an employee record by unique identifier.
     * 
     * @param id Identifier of the employee.
     * @return EmployeeDTO of the matching record.
     */
    EmployeeDTO getEmployeeById(Long id);

    /**
     * Retrieves a paginated and sorted list of employees with optional department and search filters.
     * 
     * @param pageable Page number, page size, and sorting specifications.
     * @param department Optional department name to filter by.
     * @param search Optional search term (matches first name, last name, or email).
     * @return Paginated response container holding EmployeeDTO objects.
     */
    PagedResponseDTO<EmployeeDTO> getAllEmployees(Pageable pageable, String department, String search);

    /**
     * Updates an existing employee record.
     * 
     * @param id Identifier of the employee to update.
     * @param dto Updated employee payload.
     * @return Updated EmployeeDTO instance.
     */
    EmployeeDTO updateEmployee(Long id, EmployeeDTO dto);

    /**
     * Deletes an employee record by identifier.
     * 
     * @param id Identifier of the employee to delete.
     */
    void deleteEmployee(Long id);

    /**
     * Calculates company-wide and departmental salary, headcount, and status metrics.
     * 
     * @return OverallAnalyticsDTO containing company summary and departmental breakdowns.
     */
    OverallAnalyticsDTO getDepartmentAnalytics();

    /**
     * Batch imports employee records from an uploaded CSV file with row-by-row validation.
     * 
     * @param file Uploaded Multipart CSV file.
     * @return CsvImportResponseDTO summarizing success counts, failure counts, and detailed error messages.
     */
    CsvImportResponseDTO importEmployeesFromCsv(MultipartFile file);

    /**
     * Exports employee records to CSV format, optionally filtered by department.
     * 
     * @param writer Writer stream destination.
     * @param department Optional department filter.
     */
    void exportEmployeesToCsv(Writer writer, String department);
}
