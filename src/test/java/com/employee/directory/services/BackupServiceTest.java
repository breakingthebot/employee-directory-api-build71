/*
 * services/BackupServiceTest.java
 * Unit tests for BackupServiceImpl verifying snapshot generation and listing.
 * Connects to: services/impl/BackupServiceImpl.java, repositories/EmployeeRepository.java, repositories/AuditLogRepository.java
 * Created: 2026-08-08
 */
package com.employee.directory.services;

import com.employee.directory.models.Employee;
import com.employee.directory.repositories.AuditLogRepository;
import com.employee.directory.repositories.EmployeeRepository;
import com.employee.directory.services.impl.BackupServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BackupServiceImpl backupService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("createDatabaseBackupSnapshot - Generates Backup File")
    void createDatabaseBackupSnapshot_Success() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());
        when(auditLogRepository.findAll()).thenReturn(Collections.emptyList());

        String filename = backupService.createDatabaseBackupSnapshot();

        assertNotNull(filename);
        assertTrue(filename.startsWith("snapshot_"));
        assertTrue(filename.endsWith(".json"));
    }

    @Test
    @DisplayName("listBackupSnapshots - Returns Non-Null List")
    void listBackupSnapshots_ReturnsList() {
        List<String> backups = backupService.listBackupSnapshots();
        assertNotNull(backups);
    }
}
