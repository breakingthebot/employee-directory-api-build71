/*
 * config/BackupScheduler.java
 * Scheduled task component triggering periodic database backup snapshots.
 * Connects to: services/BackupService.java, config/SchedulingConfig.java
 * Created: 2026-08-08
 */
package com.employee.directory.config;

import com.employee.directory.services.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Component scheduling periodic background database backups.
 */
@Component
public class BackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BackupScheduler.class);
    private final BackupService backupService;

    public BackupScheduler(BackupService backupService) {
        this.backupService = backupService;
    }

    /**
     * Periodic scheduled background task triggering database backup snapshots.
     * Scheduled cron: Every day at 2:00 AM ("0 0 2 * * ?").
     * Initial delay: 1 hour (3600000ms), Fixed rate: 24 hours (86400000ms).
     */
    @Scheduled(initialDelay = 3600000, fixedRate = 86400000)
    public void performScheduledBackup() {
        logger.info("Executing scheduled periodic database backup snapshot...");
        try {
            String filename = backupService.createDatabaseBackupSnapshot();
            logger.info("Scheduled backup completed successfully: {}", filename);
        } catch (Exception e) {
            logger.error("Scheduled backup failed: {}", e.getMessage(), e);
        }
    }
}
