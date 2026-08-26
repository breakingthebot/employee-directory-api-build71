/*
 * controllers/EmployeeGraphQLController.java
 * GraphQL controller mapping GraphQL query and mutation requests to EmployeeService.
 * Connects to: services/EmployeeService.java, dto/EmployeeDTO.java, schema.graphqls
 * Created: 2026-08-26
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.DepartmentAnalyticsDTO;
import com.employee.directory.dto.EmployeeDTO;
import com.employee.directory.enums.EmployeeStatus;
import com.employee.directory.services.EmployeeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller exposing GraphQL endpoints for querying and mutating employee data.
 */
@Controller
public class EmployeeGraphQLController {

    private final EmployeeService employeeService;

    public EmployeeGraphQLController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @QueryMapping
    public List<EmployeeDTO> employees(@Argument String department, @Argument Integer page, @Argument Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 50;
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return employeeService.getAllEmployees(pageable, department, null).getContent();
    }

    @QueryMapping
    public EmployeeDTO employeeById(@Argument Long id) {
        return employeeService.getEmployeeById(id);
    }

    @QueryMapping
    public List<DepartmentAnalyticsDTO> departmentAnalytics() {
        return employeeService.getDepartmentAnalytics().getDepartmentAnalytics();
    }

    @MutationMapping
    public EmployeeDTO createEmployee(@Argument Map<String, Object> input) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setFirstName((String) input.get("firstName"));
        dto.setLastName((String) input.get("lastName"));
        dto.setEmail((String) input.get("email"));
        dto.setDepartment((String) input.get("department"));
        dto.setJobTitle((String) input.get("jobTitle"));
        dto.setSalary(new BigDecimal(input.get("salary").toString()));
        dto.setHireDate(LocalDate.parse((String) input.get("hireDate")));
        dto.setStatus(EmployeeStatus.ACTIVE);
        return employeeService.createEmployee(dto);
    }

    @MutationMapping
    public Boolean deleteEmployee(@Argument Long id) {
        employeeService.deleteEmployee(id);
        return true;
    }
}
