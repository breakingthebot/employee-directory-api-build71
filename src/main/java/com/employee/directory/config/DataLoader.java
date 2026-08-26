/*
 * config/DataLoader.java
 * Sample database initializer seeding initial employee data and default user accounts into H2.
 * Connects to: repositories/EmployeeRepository.java, repositories/UserRepository.java, models/Employee.java, models/User.java
 * Created: 2026-08-08
 */
package com.employee.directory.config;

import com.employee.directory.enums.EmployeeStatus;
import com.employee.directory.enums.Role;
import com.employee.directory.models.Employee;
import com.employee.directory.models.User;
import com.employee.directory.repositories.EmployeeRepository;
import com.employee.directory.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Bootstraps sample employee data and default user accounts on application startup.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Dependency injection constructor.
     * 
     * @param employeeRepository Employee repository.
     * @param userRepository User repository.
     * @param passwordEncoder Password encoder bean.
     */
    public DataLoader(EmployeeRepository employeeRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Users for Authentication
        if (userRepository.count() == 0) {
            logger.info("Seeding default security user accounts...");
            User adminUser = new User("admin", passwordEncoder.encode("admin123"), Role.ROLE_ADMIN);
            User regularUser = new User("user", passwordEncoder.encode("user123"), Role.ROLE_USER);

            userRepository.saveAll(List.of(adminUser, regularUser));
            logger.info("Successfully seeded 'admin' (ROLE_ADMIN) and 'user' (ROLE_USER) accounts.");
        }

        // Seed Employees
        if (employeeRepository.count() == 0) {
            logger.info("Seeding sample employee data into H2 database...");

            List<Employee> seedEmployees = List.of(
                    new Employee("Alice", "Johnson", "alice.johnson@company.com", "Engineering", "Senior Software Engineer", new BigDecimal("125000.00"), LocalDate.of(2021, 3, 15), EmployeeStatus.ACTIVE),
                    new Employee("Bob", "Smith", "bob.smith@company.com", "Engineering", "Frontend Developer", new BigDecimal("95000.00"), LocalDate.of(2022, 6, 1), EmployeeStatus.ACTIVE),
                    new Employee("Carol", "Williams", "carol.williams@company.com", "Marketing", "Marketing Specialist", new BigDecimal("78000.00"), LocalDate.of(2020, 11, 10), EmployeeStatus.ACTIVE),
                    new Employee("David", "Brown", "david.brown@company.com", "Sales", "Account Executive", new BigDecimal("88000.00"), LocalDate.of(2023, 1, 20), EmployeeStatus.ACTIVE),
                    new Employee("Eva", "Davis", "eva.davis@company.com", "HR", "HR Generalist", new BigDecimal("72000.00"), LocalDate.of(2019, 8, 5), EmployeeStatus.ACTIVE),
                    new Employee("Frank", "Miller", "frank.miller@company.com", "Engineering", "DevOps Engineer", new BigDecimal("115000.00"), LocalDate.of(2022, 2, 14), EmployeeStatus.ON_LEAVE),
                    new Employee("Grace", "Wilson", "grace.wilson@company.com", "Operations", "Operations Manager", new BigDecimal("105000.00"), LocalDate.of(2018, 5, 30), EmployeeStatus.ACTIVE),
                    new Employee("Henry", "Taylor", "henry.taylor@company.com", "Sales", "Sales Representative", new BigDecimal("65000.00"), LocalDate.of(2024, 1, 8), EmployeeStatus.ACTIVE),
                    new Employee("Irene", "Anderson", "irene.anderson@company.com", "Marketing", "Content Strategist", new BigDecimal("82000.00"), LocalDate.of(2021, 9, 1), EmployeeStatus.TERMINATED),
                    new Employee("Jack", "Thomas", "jack.thomas@company.com", "Engineering", "QA Automation Lead", new BigDecimal("108000.00"), LocalDate.of(2020, 4, 12), EmployeeStatus.ACTIVE)
            );

            employeeRepository.saveAll(seedEmployees);
            logger.info("Successfully seeded {} employee records.", seedEmployees.size());
        }
    }
}
