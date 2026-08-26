/*
 * enums/Role.java
 * Enumeration representing security roles for RBAC.
 * Connects to: models/User.java, config/SecurityConfig.java
 * Created: 2026-08-08
 */
package com.employee.directory.enums;

/**
 * Security roles controlling API endpoint access permissions.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
