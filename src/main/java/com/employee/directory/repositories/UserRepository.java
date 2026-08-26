/*
 * repositories/UserRepository.java
 * Spring Data JPA Repository interface for User entity persistence operations.
 * Connects to: models/User.java, security/UserDetailsServiceImpl.java
 * Created: 2026-08-08
 */
package com.employee.directory.repositories;

import com.employee.directory.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User database access.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds user entity by unique username.
     * 
     * @param username Account username.
     * @return Optional containing User if found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a user exists with the given username.
     * 
     * @param username Account username.
     * @return true if exists, false otherwise.
     */
    boolean existsByUsername(String username);
}
