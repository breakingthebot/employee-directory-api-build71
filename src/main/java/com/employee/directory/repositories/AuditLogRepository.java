/*
 * repositories/AuditLogRepository.java
 * Repository interface for AuditLog database queries.
 * Connects to: models/AuditLog.java, services/impl/AuditServiceImpl.java
 * Created: 2026-08-08
 */
package com.employee.directory.repositories;

import com.employee.directory.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository interface for AuditLog entity persistence operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds audit records for a specific entity ordered by most recent timestamp.
     * 
     * @param entityName Entity class name.
     * @param entityId Entity identifier.
     * @return List of matching AuditLog entries.
     */
    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);

    /**
     * Retrieves all audit logs ordered by timestamp descending.
     * 
     * @param pageable Page request.
     * @return Page of AuditLog entries.
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
