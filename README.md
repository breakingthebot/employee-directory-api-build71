# Employee Directory API (Build 72)

A production-grade RESTful Employee Directory service built with Spring Boot 3.3, Java 17, Spring Data JPA, H2 Database, Spring Security 6, JWT Authentication, Bucket4j Token-Bucket Rate Limiting, Automated Database Backup Scheduler, Automated Audit Logging, Spring Cache & Caffeine In-Memory Caching, Real-Time Server-Sent Events (SSE) Streaming Engine, and OpenAPI 3.0 / Swagger UI featuring complete CRUD capabilities, multi-field pagination, dynamic sorting, keyword searching, departmental analytics, bulk CSV import/export, and Role-Based Access Control (RBAC).

## Stack
- **Language / Runtime**: Java 17 (OpenJDK)
- **Framework**: Spring Boot 3.3.0
- **Security & Auth**: Spring Security 6, JJWT (v0.12.5), BCrypt Password Hashing
- **Backups & Disaster Recovery**: Periodic `@Scheduled` background tasks, JSON snapshot serialization (`backups/`)
- **Rate Limiting**: Bucket4j Core (v8.10.1) Token-Bucket Algorithm (50 requests/min per IP)
- **Real-Time Streaming**: Server-Sent Events (`SseEmitter` broadcast publisher)
- **Caching**: Spring Cache abstraction, Caffeine In-Memory Cache Manager
- **Auditing**: Change Tracking Service (`AuditLog` entity, before/after diff summary generation)
- **Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: H2 In-Memory Database (with H2 Web Console)
- **Validation**: Jakarta / Spring Boot Starter Validation
- **Documentation**: OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui` 2.5.0)
- **Testing**: JUnit 5, Mockito, Spring Boot Starter Test, MockMvc
- **Build System**: Maven (via embedded Maven Wrapper `./mvnw`)

## Architecture Notes
The Employee Directory API is engineered following standard domain-driven separation of concerns. Persistence models (`Employee` entity) are decoupled from API contract representations (`EmployeeDTO`) to prevent data leakage and enable strict input validation at the boundary. Authentication is stateless using signed **JWT Bearer tokens** issued by `POST /api/v1/auth/login`. Requests are intercepted by `RateLimitingFilter` (Bucket4j token bucket tracking 50 requests/minute per client IP) and `JwtAuthenticationFilter`. Role-Based Access Control (RBAC) allows public read access (`GET /api/v1/employees/**`), while administrative mutations and backup triggers (`POST /api/v1/backups/create`) require `ROLE_ADMIN`. Disaster recovery is ensured via Spring `@Scheduled` background tasks (`BackupScheduler`) taking daily JSON snapshots containing employee records and audit logs. High-frequency read queries are cached via Caffeine (`@Cacheable`), while entity mutations trigger write-through cache eviction (`@CacheEvict`) and real-time SSE broadcasts (`GET /api/v1/employees/stream`). Interactive documentation is available at `/swagger-ui.html` with an embedded `Authorize` button for pasting Bearer tokens.

## Default Seed Accounts

| Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` | Full Read/Write/Delete/CSV Upload/Backup permissions |
| `user` | `user123` | `ROLE_USER` | Read-only endpoint access |

## API Endpoints & Interactive Documentation

| Method | Endpoint | Description | Auth Required | Throttling / Strategy |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/backups/create` | Trigger database backup snapshot | `ROLE_ADMIN` | Direct File Write |
| `GET` | `/api/v1/backups` | List backup snapshot files | `ROLE_ADMIN` | Direct File List |
| `POST` | `/api/v1/backups/restore` | Restore database state from snapshot | `ROLE_ADMIN` | Direct File Read |
| `GET` | `/api/v1/employees/stream` | Subscribe to real-time SSE event stream | None (Public) | Real-Time SSE Broadcast |
| `POST` | `/api/v1/auth/login` | Authenticate user & issue JWT | None (Public) | Rate Limited (50 req/min) |
| `POST` | `/api/v1/auth/register` | Register new user account | None (Public) | Rate Limited (50 req/min) |
| `GET` | `/swagger-ui.html` | Interactive Swagger UI API Browser | None (Public) | Excluded |
| `GET` | `/v3/api-docs` | Raw OpenAPI 3.0 Specification JSON | None (Public) | Excluded |
| `GET` | `/api/v1/employees` | List employees (paginated/sorted) | None (Public) | Rate Limited / Cacheable |
| `GET` | `/api/v1/employees/{id}` | Retrieve employee by ID | None (Public) | Rate Limited / Cacheable (`employees`) |
| `GET` | `/api/v1/employees/{id}/audit-history` | Retrieve employee audit history | None (Public) | Rate Limited / Direct DB |
| `GET` | `/api/v1/audit-logs` | Paginated system-wide audit logs | None (Public) | Rate Limited / Direct DB |
| `GET` | `/api/v1/employees/analytics/departments` | Departmental headcount & salary analytics | None (Public) | Rate Limited / Cacheable (`employee-analytics`) |
| `GET` | `/api/v1/employees/export` | Export employees as CSV file | None (Public) | Rate Limited / Direct Stream |
| `POST` | `/api/v1/employees` | Create a new employee | `ROLE_ADMIN` | Evicts Cache & Broadcasts Event |
| `POST` | `/api/v1/employees/upload` | Bulk import employees via CSV | `ROLE_ADMIN` | Evicts Cache & Broadcasts Events |
| `PUT` | `/api/v1/employees/{id}` | Update employee by ID | `ROLE_ADMIN` | Evicts Cache & Broadcasts Event |
| `DELETE` | `/api/v1/employees/{id}` | Delete employee by ID | `ROLE_ADMIN` | Evicts Cache & Broadcasts Event |

## Setup & Running Locally

### Prerequisites
- Java 17 JDK installed and set in environment variables (`JAVA_HOME`).
- No global Maven installation is required; the project includes the self-contained Maven Wrapper (`./mvnw` or `.\mvnw.cmd`).

### Commands

1. **Clone the repository**:
   ```bash
   git clone https://github.com/breakingthebot/employee-directory-api-build72.git
   cd employee-directory-api-build72
   ```

2. **Run tests**:
   ```bash
   ./mvnw test        # Linux / macOS
   .\mvnw.cmd test    # Windows
   ```

3. **Start the application**:
   ```bash
   ./mvnw spring-boot:run        # Linux / macOS
   .\mvnw.cmd spring-boot:run    # Windows
   ```
   The API will be available at `http://localhost:8080/api/v1/employees`.

4. **Access Swagger UI**:
   Navigate to `http://localhost:8080/swagger-ui.html` in your browser. Click **Authorize** and enter your `Bearer <token>`.

5. **Access H2 Web Console**:
   Navigate to `http://localhost:8080/h2-console` in your browser.
   - **JDBC URL**: `jdbc:h2:mem:employeedb`
   - **User Name**: `sa`
   - **Password**: `password`

## Data Handling & Privacy
- **Data Posture**: All employee data, audit records, user credentials, cache entries, and stream connections are stored in-memory during runtime and are reset on application restart. Backup snapshots are saved to `backups/`. Passwords are BCrypt hashed.
- **Sensitive Data**: Passwords, auth tokens, or PII are not persisted in plain text or logged.

## License
[MIT License](LICENSE)
