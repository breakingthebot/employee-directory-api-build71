# Iteration 8 Summary: Real-Time Event Notification Engine (WebSockets / SSE)

## Plain English Summary
In Iteration 8, we implemented a **Real-Time Event Notification Engine** using **Server-Sent Events (SSE)** for the Employee Directory API. Clients can subscribe to `GET /api/v1/employees/stream` to receive persistent, low-overhead push notifications directly from the server. Whenever an employee record is created, updated, deleted, or imported via CSV, `EmployeeEventPublisher` broadcasts structured JSON events (`EMPLOYEE_CREATED`, `EMPLOYEE_UPDATED`, `EMPLOYEE_DELETED`) containing timestamps, target employee IDs, event summaries, and payload DTOs to all active subscribers. This enables reactive administrative client dashboards to auto-update in real-time without polling the backend.

---

## File Map & Connections

| File Path | Description | Connects To |
| :--- | :--- | :--- |
| [`src/main/java/com/employee/directory/enums/EmployeeEventType.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/enums/EmployeeEventType.java) | Enum defining `EMPLOYEE_CREATED`, `EMPLOYEE_UPDATED`, and `EMPLOYEE_DELETED` stream event classifications. | [`EmployeeEventDTO.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/dto/EmployeeEventDTO.java) |
| [`src/main/java/com/employee/directory/dto/EmployeeEventDTO.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/dto/EmployeeEventDTO.java) | DTO model encapsulating event type, timestamp, employee ID, summary text, and DTO payload. | [`EmployeeEventPublisher.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/events/EmployeeEventPublisher.java) |
| [`src/main/java/com/employee/directory/events/EmployeeEventPublisher.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/events/EmployeeEventPublisher.java) | Publisher managing `SseEmitter` subscriptions and thread-safe event broadcasting. | [`EmployeeServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java), [`EmployeeController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/EmployeeController.java) |
| [`src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java) | Updated service invoking `EmployeeEventPublisher` upon creation, update, deletion, and CSV import. | [`EmployeeEventPublisher.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/events/EmployeeEventPublisher.java) |
| [`src/main/java/com/employee/directory/controllers/EmployeeController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/EmployeeController.java) | Updated controller exposing `GET /api/v1/employees/stream` producing `text/event-stream`. | [`EmployeeEventPublisher.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/events/EmployeeEventPublisher.java) |
| [`src/test/java/com/employee/directory/events/EmployeeEventPublisherTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/events/EmployeeEventPublisherTest.java) | Unit tests verifying SseEmitter subscription registration and event broadcasting. | [`EmployeeEventPublisher.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/events/EmployeeEventPublisher.java) |
| [`src/test/java/com/employee/directory/controllers/EmployeeControllerIntegrationTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/controllers/EmployeeControllerIntegrationTest.java) | Integration tests verifying `GET /api/v1/employees/stream` produces `text/event-stream`. | [`EmployeeController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/EmployeeController.java) |
| [`README.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/README.md) | Updated README with SSE streaming architecture notes and curl subscription examples. | Repository Root |
| [`CHANGELOG.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/CHANGELOG.md) | Updated technical changelog for release `[1.7.0]`. | Repository Root |
| [`BUILD_NOTES.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/BUILD_NOTES.md) | Appended Iteration 8 build log entry. | Repository Root (Ignored) |

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
   *Expected output*: `Tests run: 36, Failures: 0, Errors: 0, Skipped: 0` -> `BUILD SUCCESS`.

3. **Start the Spring Boot server**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

4. **Subscribe to Real-Time SSE Stream in Terminal A**:
   ```bash
   curl -N "http://localhost:8080/api/v1/employees/stream"
   ```
   *Expected initial output*:
   ```http
   event:INIT
   data:Connected to Employee Directory Real-Time SSE Stream
   ```

5. **Trigger Employee Creation in Terminal B**:
   First, authenticate as admin:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
   ```
   Then create a new employee:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/employees" \
        -H "Authorization: Bearer <your_jwt_token>" \
        -H "Content-Type: application/json" \
        -d "{\"firstName\":\"Diana\",\"lastName\":\"Prince\",\"email\":\"diana.prince@company.com\",\"department\":\"Operations\",\"jobTitle\":\"Director of Security\",\"salary\":160000.00,\"hireDate\":\"2022-09-01\",\"status\":\"ACTIVE\"}"
   ```

6. **Observe Stream Output in Terminal A**:
   *Expected live stream push*:
   ```http
   event:EMPLOYEE_CREATED
   data:{"eventType":"EMPLOYEE_CREATED","timestamp":"2026-08-08T14:48:00","employeeId":11,"summary":"New employee created: Diana Prince","payload":{...}}
   ```

---

## Candidate Next Iterations

### Option 1: Rate Limiting & API Throttling Filter (Bucket4j / Resilience4j) (Recommended)
- **Plain English**: Integrate Bucket4j token-bucket rate limiting to prevent API abuse by limiting clients to 100 requests per minute per IP address or user account.
- **Benefit**: Protects backend services against DDoS attacks and brute-force login attempts.
- **Trade-off**: Adds request rate counting overhead and HTTP 429 response handling.
- **Interview Answer**: *"I integrated token-bucket rate limiting using Bucket4j to throttle API abuse and protect auth endpoints against brute-force attacks."*
- **Manual Test Steps**:
  1. Send rapid bursts of HTTP GET requests to `/api/v1/employees`.
  2. Verify `HTTP 429 Too Many Requests` response after exceeding threshold.

### Option 2: Automated Database Backup & Export Scheduler (@Scheduled)
- **Plain English**: Configure a scheduled background job (`@Scheduled`) to periodically take database snapshots and export JSON/CSV dumps to target directories.
- **Benefit**: Ensures automated disaster recovery readiness and data persistence.
- **Trade-off**: Requires file lock management and disk storage monitoring.
- **Interview Answer**: *"I configured Spring `@Scheduled` background tasks to execute automated periodic database snapshots for disaster recovery."*
- **Manual Test Steps**:
  1. Trigger or wait for scheduled cron trigger.
  2. Check snapshot directory for generated backup files.

### Option 3: Soft Delete & Data Recovery Trash Bin
- **Plain English**: Update deletion logic to perform soft deletes (`is_deleted` flag or `deleted_at` timestamp) and expose `/api/v1/employees/trash` to view and restore deleted records.
- **Benefit**: Prevents accidental data loss and allows easy restoration of deleted personnel files.
- **Trade-off**: Modifies JPA query filters to exclude soft-deleted records.
- **Interview Answer**: *"I implemented soft deletion with a trash bin recovery endpoint to prevent permanent accidental data loss."*
- **Manual Test Steps**:
  1. Delete employee via `DELETE /api/v1/employees/1`.
  2. Restore employee via `POST /api/v1/employees/1/restore`.
