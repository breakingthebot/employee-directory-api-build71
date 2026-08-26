/*
 * repositories/EmployeeRepository.java
 * Spring Data JPA Repository interface for Employee entity operations.
 * Connects to: models/Employee.java, services/impl/EmployeeServiceImpl.java
 * Created: 2026-08-08
 */
package com.employee.directory.repositories;

import com.employee.directory.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository providing persistence methods and aggregate queries for Employee entities.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    /**
     * Checks if an employee with the given email address already exists.
     * 
     * @param email Email address to search for.
     * @return true if an employee exists with this email, false otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Finds employee entity by email.
     * 
     * @param email Email address.
     * @return Optional containing Employee if found.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Retrieves aggregated salary metrics grouped by department.
     * Object array indexes: [0] String department, [1] Long count, [2] Double avgSalary,
     * [3] BigDecimal minSalary, [4] BigDecimal maxSalary, [5] BigDecimal totalPayroll
     * 
     * @return List of Object arrays representing aggregate metrics per department.
     */
    @Query("SELECT e.department, COUNT(e), AVG(e.salary), MIN(e.salary), MAX(e.salary), SUM(e.salary) " +
           "FROM Employee e GROUP BY e.department ORDER BY COUNT(e) DESC, e.department ASC")
    List<Object[]> findDepartmentSalaryAnalytics();

    /**
     * Retrieves headcount grouped by department and employment status.
     * Object array indexes: [0] String department, [1] EmployeeStatus status, [2] Long count
     * 
     * @return List of Object arrays representing status counts per department.
     */
    @Query("SELECT e.department, e.status, COUNT(e) FROM Employee e GROUP BY e.department, e.status")
    List<Object[]> findDepartmentStatusCounts();
}
