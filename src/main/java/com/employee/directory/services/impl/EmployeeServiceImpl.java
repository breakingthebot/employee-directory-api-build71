/*
 * services/impl/EmployeeServiceImpl.java
 * Service implementation containing business logic, entity-to-DTO conversion, query building, analytics aggregation, CSV import/export, audit logging, caching, and real-time SSE event publishing.
 * Connects to: services/EmployeeService.java, services/AuditService.java, events/EmployeeEventPublisher.java, config/CacheConfig.java, repositories/EmployeeRepository.java, models/Employee.java, utils/CsvHelper.java
 * Created: 2026-08-08
 */
package com.employee.directory.services.impl;

import com.employee.directory.config.CacheConfig;
import com.employee.directory.dto.*;
import com.employee.directory.enums.AuditAction;
import com.employee.directory.enums.EmployeeEventType;
import com.employee.directory.enums.EmployeeStatus;
import com.employee.directory.events.EmployeeEventPublisher;
import com.employee.directory.exceptions.DuplicateResourceException;
import com.employee.directory.exceptions.ResourceNotFoundException;
import com.employee.directory.models.Employee;
import com.employee.directory.repositories.EmployeeRepository;
import com.employee.directory.services.AuditService;
import com.employee.directory.services.EmployeeService;
import com.employee.directory.utils.CsvHelper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Spring Service bean implementing business contracts for Employee management, analytics, CSV bulk processing, change auditing, caching, and real-time SSE event broadcasting.
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    private final EmployeeEventPublisher eventPublisher;

    /**
     * Dependency injection constructor.
     * 
     * @param employeeRepository Employee JPA repository.
     * @param auditService Audit service for recording entity change logs.
     * @param eventPublisher Publisher broadcasting real-time SSE notifications.
     */
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, AuditService auditService, EmployeeEventPublisher eventPublisher) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_EMPLOYEES, CacheConfig.CACHE_ANALYTICS, CacheConfig.CACHE_LISTINGS}, allEntries = true)
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + dto.getEmail() + "' already exists");
        }

        Employee employee = mapToEntity(dto);
        Employee saved = employeeRepository.save(employee);
        EmployeeDTO result = mapToDto(saved);

        auditService.logAction(
                "Employee",
                saved.getId(),
                AuditAction.CREATE,
                getCurrentUsername(),
                "Created employee record: " + saved.getFirstName() + " " + saved.getLastName() + " (" + saved.getEmail() + ")"
        );

        eventPublisher.publishEvent(new EmployeeEventDTO(
                EmployeeEventType.EMPLOYEE_CREATED,
                saved.getId(),
                "New employee created: " + saved.getFirstName() + " " + saved.getLastName(),
                result
        ));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_EMPLOYEES, key = "#id")
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<EmployeeDTO> getAllEmployees(Pageable pageable, String department, String search) {
        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(department)) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.toLowerCase().trim()));
            }

            if (StringUtils.hasText(search)) {
                String term = "%" + search.toLowerCase().trim() + "%";
                Predicate firstNameMatch = cb.like(cb.lower(root.get("firstName")), term);
                Predicate lastNameMatch = cb.like(cb.lower(root.get("lastName")), term);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), term);
                Predicate jobTitleMatch = cb.like(cb.lower(root.get("jobTitle")), term);

                predicates.add(cb.or(firstNameMatch, lastNameMatch, emailMatch, jobTitleMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Employee> page = employeeRepository.findAll(spec, pageable);
        List<EmployeeDTO> dtoList = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                dtoList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_EMPLOYEES, CacheConfig.CACHE_ANALYTICS, CacheConfig.CACHE_LISTINGS}, allEntries = true)
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Check email uniqueness if email is changed
        if (!employee.getEmail().equalsIgnoreCase(dto.getEmail()) && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + dto.getEmail() + "' already exists");
        }

        // Build field diff summary for audit log
        String diffSummary = computeDiffSummary(employee, dto);

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setJobTitle(dto.getJobTitle());
        employee.setSalary(dto.getSalary());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());

        Employee updated = employeeRepository.save(employee);
        EmployeeDTO result = mapToDto(updated);

        auditService.logAction(
                "Employee",
                id,
                AuditAction.UPDATE,
                getCurrentUsername(),
                diffSummary
        );

        eventPublisher.publishEvent(new EmployeeEventDTO(
                EmployeeEventType.EMPLOYEE_UPDATED,
                id,
                "Employee updated: " + updated.getFirstName() + " " + updated.getLastName(),
                result
        ));

        return result;
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_EMPLOYEES, CacheConfig.CACHE_ANALYTICS, CacheConfig.CACHE_LISTINGS}, allEntries = true)
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        EmployeeDTO deletedDto = mapToDto(employee);
        employeeRepository.deleteById(id);

        auditService.logAction(
                "Employee",
                id,
                AuditAction.DELETE,
                getCurrentUsername(),
                "Deleted employee record (ID: " + id + ")"
        );

        eventPublisher.publishEvent(new EmployeeEventDTO(
                EmployeeEventType.EMPLOYEE_DELETED,
                id,
                "Employee deleted: " + employee.getFirstName() + " " + employee.getLastName(),
                deletedDto
        ));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_ANALYTICS)
    public OverallAnalyticsDTO getDepartmentAnalytics() {
        List<Object[]> salaryAnalytics = employeeRepository.findDepartmentSalaryAnalytics();
        List<Object[]> statusCountsRaw = employeeRepository.findDepartmentStatusCounts();

        // Process status counts map per department
        Map<String, Map<EmployeeStatus, Long>> statusMap = new HashMap<>();
        for (Object[] row : statusCountsRaw) {
            String dept = (String) row[0];
            EmployeeStatus status = (EmployeeStatus) row[1];
            Long count = (Long) row[2];

            statusMap.computeIfAbsent(dept, k -> new EnumMap<>(EmployeeStatus.class)).put(status, count);
        }

        List<DepartmentAnalyticsDTO> deptList = new ArrayList<>();
        long totalHeadcount = 0;
        BigDecimal totalCompanyPayroll = BigDecimal.ZERO;

        for (Object[] row : salaryAnalytics) {
            String dept = (String) row[0];
            Long count = (Long) row[1];
            Double avgSalaryDouble = (Double) row[2];
            BigDecimal minSalary = (BigDecimal) row[3];
            BigDecimal maxSalary = (BigDecimal) row[4];
            BigDecimal totalPayroll = (BigDecimal) row[5];

            BigDecimal avgSalary = BigDecimal.valueOf(avgSalaryDouble != null ? avgSalaryDouble : 0.0)
                    .setScale(2, RoundingMode.HALF_UP);

            Map<EmployeeStatus, Long> deptStatuses = statusMap.getOrDefault(dept, Collections.emptyMap());

            deptList.add(new DepartmentAnalyticsDTO(
                    dept, count, avgSalary, minSalary, maxSalary, totalPayroll, deptStatuses
            ));

            totalHeadcount += count;
            if (totalPayroll != null) {
                totalCompanyPayroll = totalCompanyPayroll.add(totalPayroll);
            }
        }

        BigDecimal overallAvgSalary = totalHeadcount > 0
                ? totalCompanyPayroll.divide(BigDecimal.valueOf(totalHeadcount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new OverallAnalyticsDTO(totalHeadcount, overallAvgSalary, totalCompanyPayroll, deptList);
    }

    @Override
    @CacheEvict(value = {CacheConfig.CACHE_EMPLOYEES, CacheConfig.CACHE_ANALYTICS, CacheConfig.CACHE_LISTINGS}, allEntries = true)
    public CsvImportResponseDTO importEmployeesFromCsv(MultipartFile file) {
        if (!CsvHelper.hasCsvFormat(file)) {
            throw new IllegalArgumentException("Uploaded file must be a valid CSV document");
        }

        List<String> errors = new ArrayList<>();
        List<EmployeeDTO> importedDtos = new ArrayList<>();
        int rowNumber = 1;

        try {
            List<EmployeeDTO> parsedDtos = CsvHelper.parseCsvToEmployeeDtos(file.getInputStream());
            Set<String> processedEmailsInBatch = new HashSet<>();

            for (EmployeeDTO dto : parsedDtos) {
                rowNumber++;
                List<String> rowErrors = validateDtoForImport(dto);

                if (!rowErrors.isEmpty()) {
                    errors.add("Row " + rowNumber + ": " + String.join("; ", rowErrors));
                    continue;
                }

                if (processedEmailsInBatch.contains(dto.getEmail().toLowerCase())) {
                    errors.add("Row " + rowNumber + ": Duplicate email '" + dto.getEmail() + "' within upload batch");
                    continue;
                }

                if (employeeRepository.existsByEmail(dto.getEmail())) {
                    errors.add("Row " + rowNumber + ": Email '" + dto.getEmail() + "' already exists in database");
                    continue;
                }

                try {
                    Employee entity = mapToEntity(dto);
                    Employee saved = employeeRepository.save(entity);
                    EmployeeDTO savedDto = mapToDto(saved);
                    importedDtos.add(savedDto);
                    processedEmailsInBatch.add(dto.getEmail().toLowerCase());

                    auditService.logAction(
                            "Employee",
                            saved.getId(),
                            AuditAction.CREATE,
                            getCurrentUsername(),
                            "CSV Bulk Import: Created employee " + saved.getFirstName() + " " + saved.getLastName()
                    );

                    eventPublisher.publishEvent(new EmployeeEventDTO(
                            EmployeeEventType.EMPLOYEE_CREATED,
                            saved.getId(),
                            "CSV Import: Created employee " + saved.getFirstName() + " " + saved.getLastName(),
                            savedDto
                    ));

                } catch (Exception ex) {
                    errors.add("Row " + rowNumber + ": Database persistence failure - " + ex.getMessage());
                }
            }

            int totalProcessed = parsedDtos.size();
            int successCount = importedDtos.size();
            int failureCount = totalProcessed - successCount;

            return new CsvImportResponseDTO(totalProcessed, successCount, failureCount, errors, importedDtos);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV input stream: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exportEmployeesToCsv(Writer writer, String department) {
        Specification<Employee> spec = (root, query, cb) -> {
            if (StringUtils.hasText(department)) {
                return cb.equal(cb.lower(root.get("department")), department.toLowerCase().trim());
            }
            return cb.conjunction();
        };

        List<Employee> employees = employeeRepository.findAll(spec, Sort.by("id").ascending());
        List<EmployeeDTO> dtos = employees.stream().map(this::mapToDto).collect(Collectors.toList());

        try {
            CsvHelper.writeEmployeeDtosToCsv(writer, dtos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV export output: " + e.getMessage(), e);
        }
    }

    private String computeDiffSummary(Employee oldEntity, EmployeeDTO newDto) {
        List<String> diffs = new ArrayList<>();
        if (!Objects.equals(oldEntity.getFirstName(), newDto.getFirstName())) {
            diffs.add("firstName: '" + oldEntity.getFirstName() + "' -> '" + newDto.getFirstName() + "'");
        }
        if (!Objects.equals(oldEntity.getLastName(), newDto.getLastName())) {
            diffs.add("lastName: '" + oldEntity.getLastName() + "' -> '" + newDto.getLastName() + "'");
        }
        if (!Objects.equals(oldEntity.getEmail(), newDto.getEmail())) {
            diffs.add("email: '" + oldEntity.getEmail() + "' -> '" + newDto.getEmail() + "'");
        }
        if (!Objects.equals(oldEntity.getDepartment(), newDto.getDepartment())) {
            diffs.add("department: '" + oldEntity.getDepartment() + "' -> '" + newDto.getDepartment() + "'");
        }
        if (!Objects.equals(oldEntity.getJobTitle(), newDto.getJobTitle())) {
            diffs.add("jobTitle: '" + oldEntity.getJobTitle() + "' -> '" + newDto.getJobTitle() + "'");
        }
        if (oldEntity.getSalary() == null || newDto.getSalary() == null || oldEntity.getSalary().compareTo(newDto.getSalary()) != 0) {
            diffs.add("salary: " + oldEntity.getSalary() + " -> " + newDto.getSalary());
        }
        if (!Objects.equals(oldEntity.getHireDate(), newDto.getHireDate())) {
            diffs.add("hireDate: " + oldEntity.getHireDate() + " -> " + newDto.getHireDate());
        }
        if (oldEntity.getStatus() != newDto.getStatus()) {
            diffs.add("status: " + oldEntity.getStatus() + " -> " + newDto.getStatus());
        }

        return diffs.isEmpty() ? "No field changes detected" : String.join("; ", diffs);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "SYSTEM";
    }

    private List<String> validateDtoForImport(EmployeeDTO dto) {
        List<String> errors = new ArrayList<>();
        if (!StringUtils.hasText(dto.getFirstName())) errors.add("First name is missing");
        if (!StringUtils.hasText(dto.getLastName())) errors.add("Last name is missing");
        if (!StringUtils.hasText(dto.getEmail())) {
            errors.add("Email is missing");
        } else if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            errors.add("Invalid email format '" + dto.getEmail() + "'");
        }
        if (!StringUtils.hasText(dto.getDepartment())) errors.add("Department is missing");
        if (!StringUtils.hasText(dto.getJobTitle())) errors.add("Job title is missing");
        if (dto.getSalary() == null || dto.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Salary must be non-negative");
        }
        if (dto.getHireDate() == null) errors.add("Hire date is missing");

        return errors;
    }

    private EmployeeDTO mapToDto(Employee entity) {
        return new EmployeeDTO(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getDepartment(),
                entity.getJobTitle(),
                entity.getSalary(),
                entity.getHireDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Employee mapToEntity(EmployeeDTO dto) {
        return new Employee(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getDepartment(),
                dto.getJobTitle(),
                dto.getSalary(),
                dto.getHireDate(),
                dto.getStatus()
        );
    }
}
