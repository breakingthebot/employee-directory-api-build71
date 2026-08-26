# Iteration 5 Summary: Spring Security 6 & JWT Authentication with RBAC

## Plain English Summary
In Iteration 5, we integrated **Spring Security 6** and stateless **JWT (JSON Web Token)** authentication with **Role-Based Access Control (RBAC)** into the Employee Directory API. Users authenticate via `POST /api/v1/auth/login` to obtain a signed JWT bearer token. Unauthenticated users and regular users (`ROLE_USER`) have read-only access to directory listings, analytics, CSV exports, and Swagger documentation. Administrative operations (creating employees, updating records, deleting records, and uploading bulk CSV files) strictly require `ROLE_ADMIN` permissions. Additionally, Swagger UI has been upgraded with a `BearerAuth` security scheme so users can paste their JWT directly into the interactive browser console.

---

## File Map & Connections

| File Path | Description | Connects To |
| :--- | :--- | :--- |
| [`src/main/java/com/employee/directory/enums/Role.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/enums/Role.java) | Enum defining `ROLE_USER` and `ROLE_ADMIN` authorities. | [`User.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/models/User.java) |
| [`src/main/java/com/employee/directory/models/User.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/models/User.java) | JPA entity mapping user credentials and role assignments. | [`UserRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/UserRepository.java) |
| [`src/main/java/com/employee/directory/repositories/UserRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/UserRepository.java) | Repository providing `findByUsername` and `existsByUsername` methods. | [`UserDetailsServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/security/UserDetailsServiceImpl.java) |
| [`src/main/java/com/employee/directory/security/JwtUtils.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/security/JwtUtils.java) | Component providing JWT creation, HMAC signing, expiration check, and claim parsing. | [`JwtAuthenticationFilter.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/security/JwtAuthenticationFilter.java), [`AuthController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/AuthController.java) |
| [`src/main/java/com/employee/directory/security/UserDetailsServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/security/UserDetailsServiceImpl.java) | Implements `UserDetailsService` loading user GrantedAuthorities from database. | [`SecurityConfig.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/SecurityConfig.java) |
| [`src/main/java/com/employee/directory/security/JwtAuthenticationFilter.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/security/JwtAuthenticationFilter.java) | Intercepts HTTP requests to extract `Authorization: Bearer` headers and establish security context. | [`SecurityConfig.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/SecurityConfig.java) |
| [`src/main/java/com/employee/directory/config/SecurityConfig.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/SecurityConfig.java) | Spring Security 6 filter chain configuring permitAll endpoints, RBAC permissions, and BCrypt encoder. | [`JwtAuthenticationFilter.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/security/JwtAuthenticationFilter.java) |
| [`src/main/java/com/employee/directory/controllers/AuthController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/AuthController.java) | REST controller publishing `/api/v1/auth/login` and `/api/v1/auth/register`. | [`JwtUtils.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/security/JwtUtils.java) |
| [`src/main/java/com/employee/directory/config/DataLoader.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/DataLoader.java) | Initializer seeding default `admin` (`ROLE_ADMIN`) and `user` (`ROLE_USER`) accounts. | [`UserRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/UserRepository.java) |
| [`src/main/java/com/employee/directory/config/OpenApiConfig.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/OpenApiConfig.java) | Updated OpenAPI config adding JWT Bearer `SecurityScheme` for Swagger UI authorization. | Swagger UI |
| [`src/test/java/com/employee/directory/controllers/AuthControllerIntegrationTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/controllers/AuthControllerIntegrationTest.java) | Integration tests for authentication login, invalid password handling, and registration. | [`AuthController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/AuthController.java) |
| [`src/test/java/com/employee/directory/controllers/EmployeeControllerIntegrationTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/controllers/EmployeeControllerIntegrationTest.java) | Updated integration tests verifying `@WithMockUser(authorities = "ROLE_ADMIN")` and 403 Forbidden checks. | [`EmployeeController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/EmployeeController.java) |
| [`README.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/README.md) | Updated README with seed credentials, auth workflows, and JWT authorization documentation. | Repository Root |
| [`CHANGELOG.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/CHANGELOG.md) | Updated technical changelog for release `[1.4.0]`. | Repository Root |
| [`BUILD_NOTES.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/BUILD_NOTES.md) | Appended Iteration 5 build log entry. | Repository Root (Ignored) |

---

## Manual Testing Steps (In Another Terminal Window)

1. **Open a new terminal window** and navigate to the project directory:
   ```bash
   cd C:\Users\marve\Desktop\AI-286-Builds\Build_71
   ```

2. **Execute all unit & integration tests**:
   ```bash
   .\mvnw.cmd test
   ```
   *Expected output*: `Tests run: 26, Failures: 0, Errors: 0, Skipped: 0` -> `BUILD SUCCESS`.

3. **Start the Spring Boot server**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

4. **Test Unauthenticated Read Endpoint (Public)**:
   ```bash
   curl -X GET "http://localhost:8080/api/v1/employees/1"
   ```
   *Expected output*: Returns employee JSON record for Alice Johnson.

5. **Test Forbidden Write Endpoint Without Token**:
   ```bash
   curl -X DELETE "http://localhost:8080/api/v1/employees/1"
   ```
   *Expected output*: `403 Forbidden`.

6. **Login as Admin to Obtain JWT Token**:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
   ```
   *Expected output*: JSON response containing `"token": "eyJhbGci..."`, `"role": "ROLE_ADMIN"`.

7. **Execute Admin Mutation With Bearer Token**:
   ```bash
   curl -X DELETE "http://localhost:8080/api/v1/employees/1" \
        -H "Authorization: Bearer <your_jwt_token>"
   ```
   *Expected output*: `204 No Content`.

8. **Test Swagger UI Authorization**:
   Navigate to `http://localhost:8080/swagger-ui.html`. Click **Authorize**, enter `Bearer <your_jwt_token>`, and test `POST /api/v1/employees`.

---

## Candidate Next Iterations

### Option 1: Automated Audit Logging & Change Tracking (Recommended)
- **Plain English**: Create an `EmployeeAuditLog` JPA entity to record entity change history (who modified which field, previous value, new value, timestamp) whenever an employee is created, updated, or deleted.
- **Benefit**: Essential for compliance, tracking unauthorized edits, and historical change tracing in enterprise directory applications.
- **Trade-off**: Adds additional database write queries on every entity modification.
- **Interview Answer**: *"I built an automated JPA EntityListener audit system to capture before/after field mutations for personnel records to satisfy enterprise compliance requirements."*
- **Manual Test Steps**:
  1. Update an employee via `PUT /api/v1/employees/1`.
  2. Query `GET /api/v1/employees/1/audit-history` to view change logs.

### Option 2: Redis Multi-Level Caching & Eviction Layer
- **Plain English**: Integrate Spring Cache with Redis / Caffeine to cache paginated list queries and employee detail responses, with automatic cache eviction on create, update, or delete operations.
- **Benefit**: Dramatically reduces database load and response latency for frequently accessed directory listings.
- **Trade-off**: Introduces cache invalidation complexity and secondary storage requirements.
- **Interview Answer**: *"I implemented a multi-level caching layer using Spring Cache abstractions to serve hot directory queries from memory while enforcing write-through cache eviction on mutations."*
- **Manual Test Steps**:
  1. Request `GET /api/v1/employees/1` twice and compare execution response times.
  2. Perform `PUT /api/v1/employees/1` and confirm cache invalidation.

### Option 3: Real-Time Event Notification Engine (WebSockets / SSE)
- **Plain English**: Implement Server-Sent Events (SSE) or WebSockets at `/api/v1/employees/stream` to stream real-time push notifications to subscribed clients whenever an employee is created, updated, or deleted.
- **Benefit**: Enables reactive dashboard updates for admin users without polling the backend.
- **Trade-off**: Manages long-lived HTTP client connections and thread memory footprint.
- **Interview Answer**: *"I implemented Server-Sent Events (SSE) to broadcast reactive directory mutation events to active admin client dashboards in real-time."*
- **Manual Test Steps**:
  1. Subscribe to `GET /api/v1/employees/stream` using curl or EventSource.
  2. Perform `POST /api/v1/employees` and verify live event push.
