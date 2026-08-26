/*
 * services/EmployeeServiceTest.java
 * Unit tests for EmployeeServiceImpl verifying business logic, exceptions, analytics, and CSV import/export.
 * Connects to: services/impl/EmployeeServiceImpl.java, repositories/EmployeeRepository.java
 * Created: 2026-08-08
 */
package com.employee.directory.services;

import com.employee.directory.dto.CsvImportResponseDTO;
import com.employee.directory.dto.EmployeeDTO;
import com.employee.directory.dto.OverallAnalyticsDTO;
import com.employee.directory.dto.PagedResponseDTO;
import com.employee.directory.enums.EmployeeStatus;
import com.employee.directory.events.EmployeeEventPublisher;
import com.employee.directory.exceptions.DuplicateResourceException;
import com.employee.directory.exceptions.ResourceNotFoundException;
import com.employee.directory.models.Employee;
import com.employee.directory.repositories.EmployeeRepository;
import com.employee.directory.services.impl.EmployeeServiceImpl;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private EmployeeEventPublisher eventPublisher;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee sampleEmployee;
    private EmployeeDTO sampleDto;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee("Jane", "Doe", "jane.doe@company.com", "Engineering",
                "Backend Engineer", new BigDecimal("100000.00"), LocalDate.of(2022, 1, 15), EmployeeStatus.ACTIVE);
        sampleEmployee.setId(1L);

        sampleDto = new EmployeeDTO(null, "Jane", "Doe", "jane.doe@company.com", "Engineering",
                "Backend Engineer", new BigDecimal("100000.00"), LocalDate.of(2022, 1, 15), EmployeeStatus.ACTIVE, null, null);
    }

    @Test
    @DisplayName("createEmployee - Success")
    void createEmployee_Success() {
        when(employeeRepository.existsByEmail(sampleDto.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        EmployeeDTO result = employeeService.createEmployee(sampleDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("jane.doe@company.com", result.getEmail());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee - Throws DuplicateResourceException when email exists")
    void createEmployee_DuplicateEmail_ThrowsException() {
        when(employeeRepository.existsByEmail(sampleDto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(sampleDto));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("getEmployeeById - Success")
    void getEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    @DisplayName("getEmployeeById - Throws ResourceNotFoundException")
    void getEmployeeById_NotFound_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    @DisplayName("getAllEmployees - Returns Paginated Response")
    void getAllEmployees_ReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(sampleEmployee), pageable, 1);

        @SuppressWarnings("unchecked")
        Page<Employee> mockPage = (Page<Employee>) page;
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        PagedResponseDTO<EmployeeDTO> result = employeeService.getAllEmployees(pageable, "Engineering", "Jane");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageNumber());
    }

    @Test
    @DisplayName("deleteEmployee - Success")
    void deleteEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        doNothing().when(employeeRepository).deleteById(1L);

        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteEmployee - Throws ResourceNotFoundException when non-existent")
    void deleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(99L));
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getDepartmentAnalytics - Calculates Correct Aggregates")
    void getDepartmentAnalytics_Success() {
        List<Object[]> salaryRows = new ArrayList<>();
        salaryRows.add(new Object[]{"Engineering", 2L, 110000.0, new BigDecimal("95000.00"), new BigDecimal("125000.00"), new BigDecimal("220000.00")});

        List<Object[]> statusRows = new ArrayList<>();
        statusRows.add(new Object[]{"Engineering", EmployeeStatus.ACTIVE, 2L});

        when(employeeRepository.findDepartmentSalaryAnalytics()).thenReturn(salaryRows);
        when(employeeRepository.findDepartmentStatusCounts()).thenReturn(statusRows);

        OverallAnalyticsDTO analytics = employeeService.getDepartmentAnalytics();

        assertNotNull(analytics);
        assertEquals(2L, analytics.getTotalEmployees());
        assertEquals(new BigDecimal("220000.00"), analytics.getTotalCompanyPayroll());
        assertEquals(1, analytics.getDepartmentAnalytics().size());
        assertEquals("Engineering", analytics.getDepartmentAnalytics().get(0).getDepartment());
    }

    @Test
    @DisplayName("importEmployeesFromCsv - Process Valid CSV File")
    void importEmployeesFromCsv_Success() {
        String csvContent = "firstName,lastName,email,department,jobTitle,salary,hireDate,status\n" +
                "Tom,Hardy,tom.hardy@company.com,Security,Security Officer,95000.00,2023-04-10,ACTIVE\n";

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        when(employeeRepository.existsByEmail("tom.hardy@company.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        CsvImportResponseDTO response = employeeService.importEmployeesFromCsv(file);

        assertNotNull(response);
        assertEquals(1, response.getTotalRowsProcessed());
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    @DisplayName("exportEmployeesToCsv - Writes Valid CSV Stream")
    void exportEmployeesToCsv_Success() {
        when(employeeRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(sampleEmployee));
        StringWriter writer = new StringWriter();

        employeeService.exportEmployeesToCsv(writer, "Engineering");

        String csvOutput = writer.toString();
        assertTrue(csvOutput.contains("id,firstName,lastName,email,department,jobTitle,salary,hireDate,status"));
        assertTrue(csvOutput.contains("jane.doe@company.com"));
    }
}
