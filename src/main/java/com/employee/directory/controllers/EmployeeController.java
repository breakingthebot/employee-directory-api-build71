/*
 * controllers/EmployeeController.java
 * REST Controller exposing API endpoints for Employee Directory CRUD, pagination, sorting, analytics, CSV bulk import/export, and SSE real-time event streaming.
 * Connects to: services/EmployeeService.java, events/EmployeeEventPublisher.java, dto/EmployeeDTO.java, dto/PagedResponseDTO.java, dto/OverallAnalyticsDTO.java, dto/CsvImportResponseDTO.java
 * Created: 2026-08-08
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.*;
import com.employee.directory.events.EmployeeEventPublisher;
import com.employee.directory.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * Controller providing HTTP endpoints for employee management, analytics, CSV import/export operations, and real-time SSE streams.
 */
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Directory API", description = "Operations for managing employee records, paginated listings, search, updates, analytics, CSV bulk processing, and real-time SSE streaming")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeEventPublisher eventPublisher;

    /**
     * Dependency injection constructor.
     * 
     * @param employeeService Employee service implementation.
     * @param eventPublisher Real-time SSE event publisher.
     */
    public EmployeeController(EmployeeService employeeService, EmployeeEventPublisher eventPublisher) {
        this.employeeService = employeeService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Subscribes to real-time Server-Sent Events (SSE) stream for directory mutation notifications.
     * HTTP GET /api/v1/employees/stream
     * 
     * @return SseEmitter streaming event notifications.
     */
    @Operation(summary = "Subscribe to real-time SSE event stream", description = "Establishes a persistent Server-Sent Events (SSE) connection streaming real-time notifications whenever an employee is created, updated, or deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SSE stream established successfully",
                    content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE))
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToEvents() {
        return eventPublisher.subscribe();
    }

    /**
     * Bulk imports employee records from an uploaded CSV file.
     * HTTP POST /api/v1/employees/upload
     * 
     * @param file Uploaded CSV Multipart file.
     * @return 200 OK with CsvImportResponseDTO payload.
     */
    @Operation(summary = "Bulk import employees via CSV", description = "Parses an uploaded CSV file, executes row-by-row validation, persists valid records, and reports success and failure counts with detailed error messages.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV upload processed successfully",
                    content = @Content(schema = @Schema(implementation = CsvImportResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format or missing CSV body",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvImportResponseDTO> uploadCsvFile(
            @Parameter(description = "CSV file containing employee rows to import", required = true)
            @RequestParam("file") MultipartFile file) {

        CsvImportResponseDTO response = employeeService.importEmployeesFromCsv(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Exports employee records to a downloadable CSV file.
     * HTTP GET /api/v1/employees/export
     * 
     * @param department Optional department filter.
     * @param response HttpServletResponse output stream.
     * @throws IOException If streaming fails.
     */
    @Operation(summary = "Export employees as CSV file", description = "Generates and streams a downloadable CSV document containing all employee records or filtered by department.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV stream generated successfully",
                    content = @Content(mediaType = "text/csv"))
    })
    @GetMapping(value = "/export", produces = "text/csv")
    public void exportCsvFile(
            @Parameter(description = "Filter export records by department", example = "Engineering")
            @RequestParam(required = false) String department,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"");
        employeeService.exportEmployeesToCsv(response.getWriter(), department);
    }

    /**
     * Retrieves aggregated departmental headcount, salary statistics, and status breakdowns.
     * HTTP GET /api/v1/employees/analytics/departments
     * 
     * @return 200 OK with OverallAnalyticsDTO payload.
     */
    @Operation(summary = "Get departmental salary and headcount analytics", description = "Computes aggregate company-wide and departmental metrics including total headcount, average salary, salary ranges, total payroll expenditure, and status counts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics metrics calculated successfully",
                    content = @Content(schema = @Schema(implementation = OverallAnalyticsDTO.class)))
    })
    @GetMapping("/analytics/departments")
    public ResponseEntity<OverallAnalyticsDTO> getDepartmentAnalytics() {
        OverallAnalyticsDTO analytics = employeeService.getDepartmentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Creates a new employee record.
     * HTTP POST /api/v1/employees
     * 
     * @param dto Validated employee payload.
     * @return 201 CREATED with created EmployeeDTO.
     */
    @Operation(summary = "Create a new employee", description = "Persists a new employee record after validating input constraints and ensuring email uniqueness.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee record created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed on request body",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Employee with given email already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        EmployeeDTO created = employeeService.createEmployee(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Retrieves employee record by ID.
     * HTTP GET /api/v1/employees/{id}
     * 
     * @param id Employee identifier.
     * @return 200 OK with EmployeeDTO.
     */
    @Operation(summary = "Get employee by ID", description = "Retrieves a single employee record by its unique database identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee record found",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee record not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(
            @Parameter(description = "Employee unique ID", example = "1") @PathVariable Long id) {
        EmployeeDTO dto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieves paginated, sorted, and filtered employee records.
     * HTTP GET /api/v1/employees?page=0&size=10&sort=lastName,asc&department=Engineering&search=Alice
     * 
     * @param page Zero-based page number (default: 0).
     * @param size Page size limit (default: 10).
     * @param sort Sort parameter in format field,direction (default: id,asc).
     * @param department Optional department filter.
     * @param search Optional search query term.
     * @return 200 OK with PagedResponseDTO container.
     */
    @Operation(summary = "List employees with pagination and filters", description = "Retrieves a paginated list of employees with optional department filtering, multi-field keyword search, and flexible field sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<PagedResponseDTO<EmployeeDTO>> getAllEmployees(
            @Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting field and direction (e.g. lastName,asc or salary,desc)", example = "lastName,asc") @RequestParam(defaultValue = "id,asc") String[] sort,
            @Parameter(description = "Filter records by department name", example = "Engineering") @RequestParam(required = false) String department,
            @Parameter(description = "Search term matching first name, last name, or email", example = "Alice") @RequestParam(required = false) String search) {

        Sort sortObj = parseSortParameters(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        PagedResponseDTO<EmployeeDTO> response = employeeService.getAllEmployees(pageable, department, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing employee record.
     * HTTP PUT /api/v1/employees/{id}
     * 
     * @param id Employee identifier.
     * @param dto Validated updated payload.
     * @return 200 OK with updated EmployeeDTO.
     */
    @Operation(summary = "Update employee by ID", description = "Updates all fields of an existing employee record by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee record updated successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed on payload",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee record not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email address conflicts with another existing employee",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @Parameter(description = "Employee unique ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO dto) {

        EmployeeDTO updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an employee record by ID.
     * HTTP DELETE /api/v1/employees/{id}
     * 
     * @param id Employee identifier.
     * @return 204 NO CONTENT.
     */
    @Operation(summary = "Delete employee by ID", description = "Permanently removes an employee record from the directory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employee record deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee record not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Employee unique ID", example = "1") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSortParameters(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by("id").ascending();
        }

        String field = sort[0];
        Sort.Direction direction = Sort.Direction.ASC;

        if (sort.length > 1) {
            if ("desc".equalsIgnoreCase(sort[1])) {
                direction = Sort.Direction.DESC;
            }
        }

        return Sort.by(direction, field);
    }
}
