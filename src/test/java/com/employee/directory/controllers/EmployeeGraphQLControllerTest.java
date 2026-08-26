/*
 * controllers/EmployeeGraphQLControllerTest.java
 * Integration tests for EmployeeGraphQLController using MockMvc.
 * Connects to: controllers/EmployeeGraphQLController.java, services/EmployeeService.java
 * Created: 2026-08-26
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.EmployeeDTO;
import com.employee.directory.enums.EmployeeStatus;
import com.employee.directory.services.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeGraphQLControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        try {
            EmployeeDTO dto = new EmployeeDTO();
            dto.setFirstName("GraphQLTest");
            dto.setLastName("User");
            dto.setEmail("gql." + System.currentTimeMillis() + "@example.com");
            dto.setDepartment("Engineering");
            dto.setJobTitle("Software Engineer");
            dto.setSalary(new BigDecimal("120000.00"));
            dto.setHireDate(LocalDate.now());
            dto.setStatus(EmployeeStatus.ACTIVE);
            employeeService.createEmployee(dto);
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("GraphQL: Query all employees returns employee list")
    void testGraphQLQueryEmployees() throws Exception {
        String query = "{\"query\": \"{ employees { id firstName lastName email department jobTitle } }\"}";

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employees").isArray());
    }

    @Test
    @DisplayName("GraphQL: Query department analytics returns metrics")
    void testGraphQLQueryDepartmentAnalytics() throws Exception {
        String query = "{\"query\": \"{ departmentAnalytics { department employeeCount averageSalary } }\"}";

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departmentAnalytics").isArray());
    }
}
