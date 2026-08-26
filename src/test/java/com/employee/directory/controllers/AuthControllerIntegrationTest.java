/*
 * controllers/AuthControllerIntegrationTest.java
 * Integration tests for AuthController verifying login authentication and JWT token generation.
 * Connects to: controllers/AuthController.java, security/JwtUtils.java
 * Created: 2026-08-08
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.AuthRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/auth/login - Admin Login Returns JWT Token")
    void login_AdminSuccess_ReturnsToken() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO("admin", "admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 401 Unauthorized on invalid credentials")
    void login_InvalidPassword_Returns401() throws Exception {
        AuthRequestDTO loginRequest = new AuthRequestDTO("admin", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Register new user successfully")
    void register_Success() throws Exception {
        AuthRequestDTO registerRequest = new AuthRequestDTO("newuser", "securepass");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully!"));
    }
}
