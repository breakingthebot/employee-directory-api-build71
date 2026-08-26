/*
 * dto/AuthResponseDTO.java
 * DTO carrying JWT authentication token and user identity details.
 * Connects to: controllers/AuthController.java
 * Created: 2026-08-08
 */
package com.employee.directory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload model returned upon successful authentication.
 */
@Schema(description = "Authentication token response model")
public class AuthResponseDTO {

    @Schema(description = "Generated JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Token type prefix", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated username", example = "admin")
    private String username;

    @Schema(description = "Assigned user role authority", example = "ROLE_ADMIN")
    private String role;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
