/*
 * config/LiquibaseIntegrationTest.java
 * Verification tests checking Liquibase migrations execute cleanly and populate schema tables.
 * Connects to: db/changelog/db.changelog-master.xml, repositories/UserRepository.java
 * Created: 2026-08-26
 */
package com.employee.directory.config;

import com.employee.directory.models.User;
import com.employee.directory.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LiquibaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Liquibase: Migration seeds admin_lq user into database")
    void testLiquibaseSeededUser() {
        Optional<User> user = userRepository.findByUsername("admin_lq");
        assertTrue(user.isPresent(), "Expected Liquibase changeSet 002 to seed admin_lq user");
    }
}
