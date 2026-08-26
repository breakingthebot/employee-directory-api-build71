/*
 * services/AuditServiceTest.java
 * Unit tests for AuditServiceImpl verifying audit log creation and query retrieval.
 * Connects to: services/impl/AuditServiceImpl.java, repositories/AuditLogRepository.java
 * Created: 2026-08-08
 */
package com.employee.directory.services;

import com.employee.directory.dto.AuditLogDTO;
import com.employee.directory.dto.PagedResponseDTO;
import com.employee.directory.enums.AuditAction;
import com.employee.directory.models.AuditLog;
import com.employee.directory.repositories.AuditLogRepository;
import com.employee.directory.services.impl.AuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog sampleAuditLog;

    @BeforeEach
    void setUp() {
        sampleAuditLog = new AuditLog("Employee", 1L, AuditAction.UPDATE, "admin", "lastName: Johnson -> Johnson-Smith");
        sampleAuditLog.setId(10L);
    }

    @Test
    @DisplayName("logAction - Persists AuditLog Entity")
    void logAction_Success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(sampleAuditLog);

        auditService.logAction("Employee", 1L, AuditAction.UPDATE, "admin", "lastName: Johnson -> Johnson-Smith");

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("getAuditHistoryForEmployee - Returns DTO List")
    void getAuditHistoryForEmployee_ReturnsList() {
        when(auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc("Employee", 1L))
                .thenReturn(List.of(sampleAuditLog));

        List<AuditLogDTO> history = auditService.getAuditHistoryForEmployee(1L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("Employee", history.get(0).getEntityName());
        assertEquals(AuditAction.UPDATE, history.get(0).getAction());
        assertEquals("admin", history.get(0).getModifiedBy());
    }

    @Test
    @DisplayName("getAllAuditLogs - Returns Paginated Audit Logs")
    void getAllAuditLogs_ReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog), pageable, 1);
        when(auditLogRepository.findAllByOrderByTimestampDesc(pageable)).thenReturn(page);

        PagedResponseDTO<AuditLogDTO> response = auditService.getAllAuditLogs(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }
}
