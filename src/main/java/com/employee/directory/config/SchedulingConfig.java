/*
 * config/SchedulingConfig.java
 * Configuration bean enabling Spring background task scheduling.
 * Connects to: config/BackupScheduler.java
 * Created: 2026-08-08
 */
package com.employee.directory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring task scheduling for periodic database backups and background tasks.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
