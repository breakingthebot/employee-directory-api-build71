/*
 * controllers/BackupControllerIntegrationTest.java
 * Integration tests for BackupController verifying backup creation and listing with admin security context.
 * Connects to: controllers/BackupController.java, services/BackupService.java
 * Created: 2026-08-08
 */
package com.employee.directory.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BackupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("POST /api/v1/backups/create - Create backup snapshot as admin")
    void createBackup_Success() throws Exception {
        mockMvc.perform(post("/api/v1/backups/create"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("created successfully")))
                .andExpect(jsonPath("$.filename", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("GET /api/v1/backups - List backup snapshots as admin")
    void listBackups_Success() throws Exception {
        mockMvc.perform(get("/api/v1/backups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/backups/create - 403 Forbidden without admin role")
    void createBackup_ForbiddenWithoutAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/backups/create"))
                .andExpect(status().isForbidden());
    }
}
