/*
 * controllers/AuthController.java
 * REST controller managing user authentication and JWT token generation.
 * Connects to: security/JwtUtils.java, repositories/UserRepository.java, dto/AuthRequestDTO.java, dto/AuthResponseDTO.java
 * Created: 2026-08-08
 */
package com.employee.directory.controllers;

import com.employee.directory.dto.ApiErrorResponse;
import com.employee.directory.dto.AuthRequestDTO;
import com.employee.directory.dto.AuthResponseDTO;
import com.employee.directory.enums.Role;
import com.employee.directory.models.User;
import com.employee.directory.repositories.UserRepository;
import com.employee.directory.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing authentication endpoints for logging in and receiving JWT bearer tokens.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for user login and JWT bearer token issuance")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Authenticates user credentials and issues a JWT token.
     * HTTP POST /api/v1/auth/login
     * 
     * @param loginRequest Validated login credentials.
     * @return 200 OK with AuthResponseDTO payload.
     */
    @Operation(summary = "Login and obtain JWT token", description = "Authenticates username and password against the database and returns a signed JWT bearer token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials or invalid username/password",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return ResponseEntity.ok(new AuthResponseDTO(jwt, userDetails.getUsername(), role));
    }

    /**
     * Registers a new user account (default ROLE_USER).
     * HTTP POST /api/v1/auth/register
     * 
     * @param registerRequest Credentials for new user.
     * @return 201 CREATED response message.
     */
    @Operation(summary = "Register a new user account", description = "Registers a new user with BCrypt password hashing and ROLE_USER permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Username already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody AuthRequestDTO registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        User user = new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                Role.ROLE_USER
        );

        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }
}
