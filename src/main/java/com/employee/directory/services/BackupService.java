/*
 * services/BackupService.java
 * Service interface for automated database backup snapshot creation and restoration.
 * Connects to: controllers/BackupController.java, config/BackupScheduler.java
 * Created: 2026-08-08
 */
package com.employee.directory.services;

import java.util.List;

/**
 * Service contract for managing database backup snapshots.
 */
public interface BackupService {

    /**
     * Creates a new JSON database backup snapshot file containing all employee records and audit logs.
     * 
     * @return Generated snapshot filename.
     */
    String createDatabaseBackupSnapshot();

    /**
     * Lists all available backup snapshot filenames in the backups directory.
     * 
     * @return List of snapshot filenames.
     */
    List<String> listBackupSnapshots();

    /**
     * Restores database state from a specified backup snapshot file.
     * 
     * @param filename Target snapshot filename.
     * @return true if restoration succeeded.
     */
    boolean restoreBackupSnapshot(String filename);
}
