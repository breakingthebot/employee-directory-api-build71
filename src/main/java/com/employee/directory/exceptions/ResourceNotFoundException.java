/*
 * exceptions/ResourceNotFoundException.java
 * Runtime exception thrown when a requested resource is not found.
 * Connects to: services/impl/EmployeeServiceImpl.java, exceptions/GlobalExceptionHandler.java
 * Created: 2026-08-08
 */
package com.employee.directory.exceptions;

/**
 * Exception for missing database resources.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs exception with message.
     * 
     * @param message Detailed error message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
