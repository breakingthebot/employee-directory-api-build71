/*
 * controllers/EmployeeControllerIntegrationTest.java
 * Integration tests for EmployeeController using MockMvc, Spring Security, and in-memory H2 database.
 * Connects to: controllers/EmployeeController.java, services/EmployeeService.java
 * Created: 2026-08-08
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.EmployeeDTO;
import com.employee.directory.enums.EmployeeStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/employees/stream - Connect to SSE event stream")
    void subscribeToEvents_ReturnsSseStream() throws Exception {
        mockMvc.perform(get("/api/v1/employees/stream"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("POST /api/v1/employees/upload - Upload and batch import CSV")
    void uploadCsvFile_Success() throws Exception {
        String csvData = "firstName,lastName,email,department,jobTitle,salary,hireDate,status\n" +
                "Peter,Parker,peter.parker@company.com,Photography,Photojournalist,75000.00,2023-01-15,ACTIVE\n";

        MockMultipartFile multipartFile = new MockMultipartFile("file", "employees.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/employees/upload").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRowsProcessed", is(1)))
                .andExpect(jsonPath("$.successCount", is(1)))
                .andExpect(jsonPath("$.failureCount", is(0)));
    }

    @Test
    @DisplayName("GET /api/v1/employees/export - Stream employee directory CSV")
    void exportCsvFile_ReturnsCsvContent() throws Exception {
        mockMvc.perform(get("/api/v1/employees/export").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"employees.csv\"")))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("alice.johnson@company.com")));
    }

    @Test
    @DisplayName("GET /api/v1/employees/analytics/departments - Return departmental analytics")
    void getDepartmentAnalytics_ReturnsCalculatedMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/employees/analytics/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees", is(10)))
                .andExpect(jsonPath("$.overallAverageSalary", notNullValue()))
                .andExpect(jsonPath("$.totalCompanyPayroll", is(933000.00)))
                .andExpect(jsonPath("$.departmentAnalytics", hasSize(5)))
                .andExpect(jsonPath("$.departmentAnalytics[0].department", is("Engineering")))
                .andExpect(jsonPath("$.departmentAnalytics[0].employeeCount", is(4)));
    }

    @Test
    @DisplayName("GET /api/v1/employees - Return seeded employees paginated")
    void getAllEmployees_ReturnsPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(5)));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} - Return employee by ID")
    void getEmployeeById_ReturnsEmployee() throws Exception {
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("alice.johnson@company.com")));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} - 404 when non-existent")
    void getEmployeeById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/employees/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("POST /api/v1/employees - Create new employee successfully")
    void createEmployee_Success() throws Exception {
        EmployeeDTO dto = new EmployeeDTO(null, "Mark", "Ruffalo", "mark.ruffalo@company.com", "Design",
                "UI Designer", new BigDecimal("92000.00"), LocalDate.of(2023, 5, 10), EmployeeStatus.ACTIVE, null, null);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("Mark")))
                .andExpect(jsonPath("$.email", is("mark.ruffalo@company.com")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("POST /api/v1/employees - 400 Bad Request on validation failure")
    void createEmployee_ValidationFailure_Returns400() throws Exception {
        EmployeeDTO dto = new EmployeeDTO(null, "", "", "invalid-email", "",
                "", new BigDecimal("-100.00"), LocalDate.now().plusDays(1), null, null, null);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details.email", notNullValue()))
                .andExpect(jsonPath("$.details.firstName", notNullValue()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("PUT /api/v1/employees/{id} - Update existing employee")
    void updateEmployee_Success() throws Exception {
        EmployeeDTO dto = new EmployeeDTO(null, "Alice", "Johnson-Smith", "alice.johnson@company.com", "Engineering",
                "Principal Software Engineer", new BigDecimal("145000.00"), LocalDate.of(2021, 3, 15), EmployeeStatus.ACTIVE, null, null);

        mockMvc.perform(put("/api/v1/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.lastName", is("Johnson-Smith")))
                .andExpect(jsonPath("$.jobTitle", is("Principal Software Engineer")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("DELETE /api/v1/employees/{id} - Delete employee record")
    void deleteEmployee_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/employees - 403 Forbidden without ROLE_ADMIN")
    void createEmployee_ForbiddenWithoutAdminRole() throws Exception {
        EmployeeDTO dto = new EmployeeDTO(null, "Mark", "Ruffalo", "mark.ruffalo@company.com", "Design",
                "UI Designer", new BigDecimal("92000.00"), LocalDate.of(2023, 5, 10), EmployeeStatus.ACTIVE, null, null);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
