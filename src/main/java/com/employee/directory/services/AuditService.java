/*
 * services/AuditService.java
 * Service interface for recording and querying audit logs.
 * Connects to: dto/AuditLogDTO.java, dto/PagedResponseDTO.java
 * Created: 2026-08-08
 */
package com.employee.directory.services;

import com.employee.directory.dto.AuditLogDTO;
import com.employee.directory.dto.PagedResponseDTO;
import com.employee.directory.enums.AuditAction;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract for managing application change audits.
 */
public interface AuditService {

    /**
     * Creates and saves a new audit log entry.
     * 
     * @param entityName Target entity name.
     * @param entityId Target entity ID.
     * @param action Mutation action type.
     * @param modifiedBy User executing the change.
     * @param changeSummary Detailed diff of field changes.
     */
    void logAction(String entityName, Long entityId, AuditAction action, String modifiedBy, String changeSummary);

    /**
     * Retrieves audit log history for a specific employee ID.
     * 
     * @param employeeId Employee identifier.
     * @return List of AuditLogDTO entries.
     */
    List<AuditLogDTO> getAuditHistoryForEmployee(Long employeeId);

    /**
     * Retrieves paginated audit logs across all system entities.
     * 
     * @param pageable Page request.
     * @return Paginated audit log response.
     */
    PagedResponseDTO<AuditLogDTO> getAllAuditLogs(Pageable pageable);
}
