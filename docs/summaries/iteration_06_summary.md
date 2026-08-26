# Iteration 6 Summary: Automated Audit Logging & Change Tracking

## Plain English Summary
In Iteration 6, we implemented an **Automated Audit Logging and Change Tracking Engine** for the Employee Directory API. Every creation, modification, or deletion of an employee record is automatically captured into immutable `AuditLog` database records (`audit_logs` table). When an employee record is updated, the system computes field-level diffs comparing the existing entity against the new payload (e.g. `lastName: 'Johnson' -> 'Johnson-Smith'; salary: 125000.00 -> 145000.00`) and records the authenticated username from the SecurityContext. Historical audit logs can be queried per employee via `GET /api/v1/employees/{id}/audit-history` or system-wide via `GET /api/v1/audit-logs`.

---

## File Map & Connections

| File Path | Description | Connects To |
| :--- | :--- | :--- |
| [`src/main/java/com/employee/directory/enums/AuditAction.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/enums/AuditAction.java) | Enum defining `CREATE`, `UPDATE`, and `DELETE` audit action types. | [`AuditLog.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/models/AuditLog.java) |
| [`src/main/java/com/employee/directory/models/AuditLog.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/models/AuditLog.java) | JPA entity capturing audit records, target entity ID, action type, caller username, timestamp, and field diff summary. | [`AuditLogRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/AuditLogRepository.java) |
| [`src/main/java/com/employee/directory/dto/AuditLogDTO.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/dto/AuditLogDTO.java) | DTO model returning audit log data in REST API responses. | [`AuditController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/AuditController.java) |
| [`src/main/java/com/employee/directory/repositories/AuditLogRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/AuditLogRepository.java) | Spring Data JPA repository for querying audit records by entity ID or timestamp order. | [`AuditServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/AuditServiceImpl.java) |
| [`src/main/java/com/employee/directory/services/AuditService.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/AuditService.java) | Interface declaring `logAction`, `getAuditHistoryForEmployee`, and `getAllAuditLogs`. | [`EmployeeServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java), [`AuditController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/AuditController.java) |
| [`src/main/java/com/employee/directory/services/impl/AuditServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/AuditServiceImpl.java) | Service implementation storing audit records and converting entities to DTOs. | [`AuditLogRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/AuditLogRepository.java) |
| [`src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java) | Updated service invoking `AuditService` on entity mutations and computing before/after field diff summaries. | [`AuditService.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/AuditService.java) |
| [`src/main/java/com/employee/directory/controllers/AuditController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/AuditController.java) | REST controller publishing `/api/v1/employees/{id}/audit-history` and `/api/v1/audit-logs`. | [`AuditService.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/AuditService.java) |
| [`src/main/java/com/employee/directory/config/SecurityConfig.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/SecurityConfig.java) | Updated security config permitting read-only access to audit log endpoints. | Security Filter Chain |
| [`src/test/java/com/employee/directory/services/AuditServiceTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/services/AuditServiceTest.java) | Unit tests verifying `AuditServiceImpl` logging and query retrieval. | [`AuditServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/AuditServiceImpl.java) |
| [`src/test/java/com/employee/directory/controllers/AuditControllerIntegrationTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/controllers/AuditControllerIntegrationTest.java) | Integration tests verifying audit log creation upon employee update and history endpoint response. | [`AuditController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/AuditController.java) |
| [`README.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/README.md) | Updated README with audit logging endpoint documentation and JSON output examples. | Repository Root |
| [`CHANGELOG.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/CHANGELOG.md) | Updated technical changelog for release `[1.5.0]`. | Repository Root |
| [`BUILD_NOTES.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/BUILD_NOTES.md) | Appended Iteration 6 build log entry. | Repository Root (Ignored) |

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
   *Expected output*: `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0` -> `BUILD SUCCESS`.

3. **Start the Spring Boot server**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

4. **Authenticate as Admin**:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
   ```
   *Save the returned Bearer token.*

5. **Perform an Employee Update**:
   ```bash
   curl -X PUT "http://localhost:8080/api/v1/employees/1" \
        -H "Authorization: Bearer <your_jwt_token>" \
        -H "Content-Type: application/json" \
        -d "{\"firstName\":\"Alice\",\"lastName\":\"Johnson-Smith\",\"email\":\"alice.johnson@company.com\",\"department\":\"Engineering\",\"jobTitle\":\"Principal Software Engineer\",\"salary\":150000.00,\"hireDate\":\"2021-03-15\",\"status\":\"ACTIVE\"}"
   ```

6. **Query Audit History for Employee ID 1**:
   ```bash
   curl -X GET "http://localhost:8080/api/v1/employees/1/audit-history"
   ```
   *Expected output*: Returns list of `AuditLogDTO` objects showing `action: "UPDATE"`, `modifiedBy: "admin"`, and `changeSummary` detailing `lastName` and `salary` diffs.

7. **Query System-Wide Audit Logs**:
   ```bash
   curl -X GET "http://localhost:8080/api/v1/audit-logs"
   ```
   *Expected output*: Paginated list of all audit trail entries sorted by timestamp descending.

---

## Candidate Next Iterations

### Option 1: Redis Multi-Level Caching & Eviction Layer (Recommended)
- **Plain English**: Integrate Spring Cache with Redis / Caffeine to cache paginated list queries and employee detail responses, with automatic cache eviction on create, update, or delete operations.
- **Benefit**: Dramatically reduces database load and response latency for frequently accessed directory listings.
- **Trade-off**: Introduces cache invalidation complexity and secondary storage requirements.
- **Interview Answer**: *"I implemented a multi-level caching layer using Spring Cache abstractions to serve hot directory queries from memory while enforcing write-through cache eviction on mutations."*
- **Manual Test Steps**:
  1. Request `GET /api/v1/employees/1` twice and compare execution response times.
  2. Perform `PUT /api/v1/employees/1` and confirm cache invalidation.

### Option 2: Real-Time Event Notification Engine (WebSockets / SSE)
- **Plain English**: Implement Server-Sent Events (SSE) or WebSockets at `/api/v1/employees/stream` to stream real-time push notifications to subscribed clients whenever an employee is created, updated, or deleted.
- **Benefit**: Enables reactive dashboard updates for admin users without polling the backend.
- **Trade-off**: Manages long-lived HTTP client connections and thread memory footprint.
- **Interview Answer**: *"I implemented Server-Sent Events (SSE) to broadcast reactive directory mutation events to active admin client dashboards in real-time."*
- **Manual Test Steps**:
  1. Subscribe to `GET /api/v1/employees/stream` using curl or EventSource.
  2. Perform `POST /api/v1/employees` and verify live event push.

### Option 3: Rate Limiting & API Throttling Filter (Bucket4j / Resilience4j)
- **Plain English**: Integrate Bucket4j token-bucket rate limiting to prevent API abuse by limiting clients to 100 requests per minute per IP address or user account.
- **Benefit**: Protects backend services against DDoS attacks and brute-force login attempts.
- **Trade-off**: Adds request rate counting overhead and HTTP 429 response handling.
- **Interview Answer**: *"I integrated token-bucket rate limiting using Bucket4j to throttle API abuse and protect auth endpoints against brute-force attacks."*
- **Manual Test Steps**:
  1. Send rapid bursts of HTTP GET requests to `/api/v1/employees`.
  2. Verify `HTTP 429 Too Many Requests` response after exceeding threshold.
