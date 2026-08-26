/*
 * EmployeeDirectoryApplication.java
 * Main application entry point for the Employee Directory Spring Boot Application.
 * Connects to: Spring Boot Framework, Spring Application Context
 * Created: 2026-08-08
 */
package com.employee.directory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class responsible for launching the Spring Boot Employee Directory API.
 */
@SpringBootApplication
public class EmployeeDirectoryApplication {

    /**
     * Main entry method executing SpringApplication.run.
     * 
     * @param args Command-line arguments passed at runtime.
     */
    public static void main(String[] args) {
        SpringApplication.run(EmployeeDirectoryApplication.class, args);
    }
}
