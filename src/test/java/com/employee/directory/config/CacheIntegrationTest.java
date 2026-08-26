/*
 * config/CacheIntegrationTest.java
 * Integration test verifying Spring Cache operations and Caffeine cache eviction on mutations.
 * Connects to: config/CacheConfig.java, services/EmployeeService.java, repositories/EmployeeRepository.java
 * Created: 2026-08-08
 */
package com.employee.directory.config;

import com.employee.directory.dto.EmployeeDTO;
import com.employee.directory.enums.EmployeeStatus;
import com.employee.directory.repositories.EmployeeRepository;
import com.employee.directory.services.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class CacheIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @SpyBean
    private EmployeeRepository employeeRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Cacheable - Repeated GET by ID uses cached result without re-querying DB")
    void getEmployeeById_UsesCacheOnRepeatedCalls() {
        if (cacheManager.getCache(CacheConfig.CACHE_EMPLOYEES) != null) {
            cacheManager.getCache(CacheConfig.CACHE_EMPLOYEES).clear();
        }

        // First invocation - should query DB repository
        EmployeeDTO first = employeeService.getEmployeeById(1L);
        assertNotNull(first);

        // Second invocation - should return cached instance
        EmployeeDTO second = employeeService.getEmployeeById(1L);
        assertNotNull(second);

        // Verify repository findById was only called once due to caching
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    @DisplayName("CacheEvict - Updating employee clears cache entries")
    void updateEmployee_EvictsCache() {
        if (cacheManager.getCache(CacheConfig.CACHE_EMPLOYEES) != null) {
            cacheManager.getCache(CacheConfig.CACHE_EMPLOYEES).clear();
        }

        // Populate cache
        employeeService.getEmployeeById(1L);

        EmployeeDTO updateDto = new EmployeeDTO(null, "Alice", "Johnson-Smith", "alice.johnson@company.com", "Engineering",
                "Principal Software Engineer", new BigDecimal("150000.00"), LocalDate.of(2021, 3, 15), EmployeeStatus.ACTIVE, null, null);

        // Perform mutation - triggers @CacheEvict
        employeeService.updateEmployee(1L, updateDto);

        // Subsequent call should query DB repository again after cache eviction
        EmployeeDTO result = employeeService.getEmployeeById(1L);
        assertNotNull(result);

        // Verify repository findById was queried after cache eviction
        verify(employeeRepository, atLeast(2)).findById(1L);
    }
}
