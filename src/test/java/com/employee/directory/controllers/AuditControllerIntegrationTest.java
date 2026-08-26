/*
 * controllers/AuditControllerIntegrationTest.java
 * Integration tests for AuditController verifying audit trail creation and REST endpoint retrieval.
 * Connects to: controllers/AuditController.java, services/AuditService.java
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("GET /api/v1/employees/{id}/audit-history - Return audit trail after update")
    void getAuditHistory_ReturnsLogsAfterMutation() throws Exception {
        EmployeeDTO updateDto = new EmployeeDTO(null, "Alice", "Johnson-Williams", "alice.johnson@company.com", "Engineering",
                "Principal Architect", new BigDecimal("155000.00"), LocalDate.of(2021, 3, 15), EmployeeStatus.ACTIVE, null, null);

        // Perform mutation to generate audit log
        mockMvc.perform(put("/api/v1/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        // Verify audit history log was created
        mockMvc.perform(get("/api/v1/employees/1/audit-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].entityName", is("Employee")))
                .andExpect(jsonPath("$[0].action", is("UPDATE")))
                .andExpect(jsonPath("$[0].modifiedBy", is("admin")))
                .andExpect(jsonPath("$[0].changeSummary", containsString("Johnson-Williams")));
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs - Return paginated audit logs")
    void getAllAuditLogs_ReturnsPagedResults() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.pageNumber", is(0)));
    }
}
