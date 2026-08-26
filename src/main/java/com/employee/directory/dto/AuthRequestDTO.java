/*
 * dto/AuthRequestDTO.java
 * DTO carrying login credentials.
 * Connects to: controllers/AuthController.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload model for login requests.
 */
@Schema(description = "Authentication login request model")
public class AuthRequestDTO {

    @NotBlank(message = "Username is required")
    @Schema(description = "User account username", example = "admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "User account password", example = "admin123")
    private String password;

    public AuthRequestDTO() {
    }

    public AuthRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
