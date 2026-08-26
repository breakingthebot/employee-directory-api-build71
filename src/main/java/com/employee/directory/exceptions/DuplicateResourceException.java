/*
 * exceptions/DuplicateResourceException.java
 * Runtime exception thrown when a resource violates unique constraints (e.g. duplicate email).
 * Connects to: services/impl/EmployeeServiceImpl.java, exceptions/GlobalExceptionHandler.java
 * Created: 2026-08-08
 */
package com.employee.directory.exceptions;

/**
 * Exception for duplicate resource constraints.
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs exception with message.
     * 
     * @param message Detailed error message.
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
