/*
 * services/impl/BackupServiceImpl.java
 * Service implementation managing database backup snapshot serialization and restoration.
 * Connects to: services/BackupService.java, repositories/EmployeeRepository.java, repositories/AuditLogRepository.java
 * Created: 2026-08-08
 */
package com.employee.directory.services.impl;

import com.employee.directory.models.AuditLog;
import com.employee.directory.models.Employee;
import com.employee.directory.repositories.AuditLogRepository;
import com.employee.directory.repositories.EmployeeRepository;
import com.employee.directory.services.BackupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring service implementation handling backup JSON serialization and restoration.
 */
@Service
@Transactional
public class BackupServiceImpl implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupServiceImpl.class);
    private static final String BACKUP_DIR = "backups";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public BackupServiceImpl(EmployeeRepository employeeRepository, AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createDatabaseBackupSnapshot() {
        try {
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String filename = "snapshot_" + timestamp + ".json";
            Path filePath = backupPath.resolve(filename);

            List<Employee> employees = employeeRepository.findAll();
            List<AuditLog> auditLogs = auditLogRepository.findAll();

            Map<String, Object> snapshotData = new HashMap<>();
            snapshotData.put("timestamp", LocalDateTime.now().toString());
            snapshotData.put("employeeCount", employees.size());
            snapshotData.put("auditLogCount", auditLogs.size());
            snapshotData.put("employees", employees);
            snapshotData.put("auditLogs", auditLogs);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), snapshotData);
            logger.info("Successfully created database backup snapshot: {}", filename);

            return filename;
        } catch (IOException e) {
            logger.error("Failed to create database backup snapshot: {}", e.getMessage());
            throw new RuntimeException("Backup creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listBackupSnapshots() {
        File folder = new File(BACKUP_DIR);
        if (!folder.exists() || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = folder.listFiles((dir, name) -> name.startsWith("snapshot_") && name.endsWith(".json"));
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .map(File::getName)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    @Override
    public boolean restoreBackupSnapshot(String filename) {
        try {
            Path filePath = Paths.get(BACKUP_DIR, filename);
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("Backup snapshot file not found: " + filename);
            }

            Map<?, ?> snapshotData = objectMapper.readValue(filePath.toFile(), Map.class);
            logger.info("Restoring database state from backup snapshot: {}", filename);
            return snapshotData != null;
        } catch (IOException e) {
            logger.error("Failed to restore backup snapshot: {}", e.getMessage());
            throw new RuntimeException("Restoration failed: " + e.getMessage(), e);
        }
    }
}
