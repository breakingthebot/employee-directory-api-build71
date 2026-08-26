/*
 * services/impl/AuditServiceImpl.java
 * Service implementation for audit log persistence and history querying.
 * Connects to: services/AuditService.java, repositories/AuditLogRepository.java, models/AuditLog.java
 * Created: 2026-08-08
 */
package com.employee.directory.services.impl;

import com.employee.directory.dto.AuditLogDTO;
import com.employee.directory.dto.PagedResponseDTO;
import com.employee.directory.enums.AuditAction;
import com.employee.directory.models.AuditLog;
import com.employee.directory.repositories.AuditLogRepository;
import com.employee.directory.services.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring service implementation handling audit persistence and history retrieval.
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void logAction(String entityName, Long entityId, AuditAction action, String modifiedBy, String changeSummary) {
        AuditLog auditLog = new AuditLog(entityName, entityId, action, modifiedBy, changeSummary);
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditHistoryForEmployee(Long employeeId) {
        List<AuditLog> logs = auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc("Employee", employeeId);
        return logs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<AuditLogDTO> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        List<AuditLogDTO> dtoList = page.getContent().stream().map(this::mapToDto).collect(Collectors.toList());

        return new PagedResponseDTO<>(
                dtoList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private AuditLogDTO mapToDto(AuditLog entity) {
        return new AuditLogDTO(
                entity.getId(),
                entity.getEntityName(),
                entity.getEntityId(),
                entity.getAction(),
                entity.getModifiedBy(),
                entity.getTimestamp(),
                entity.getChangeSummary()
        );
    }
}
