# Iteration 10 Summary: Automated Database Backup & Snapshot Scheduler (@Scheduled)

## Plain English Summary
In Iteration 10, we implemented an **Automated Database Backup & Snapshot Scheduler** to ensure disaster recovery readiness and data persistence for the Employee Directory API. Enabling Spring task scheduling via `SchedulingConfig` allowed `BackupScheduler` to periodically trigger background backups (`@Scheduled`). `BackupServiceImpl` serializes all current `Employee` records and system `AuditLog` history into timestamped JSON snapshot files saved to the `backups/` directory (`snapshot_YYYYMMDD_HHmmss.json`). Additionally, we published `BackupController` endpoints (`POST /api/v1/backups/create`, `GET /api/v1/backups`, `POST /api/v1/backups/restore`) restricted to `ROLE_ADMIN` users to allow manual snapshot generation and database restoration on demand.

---

## File Map & Connections

| File Path | Description | Connects To |
| :--- | :--- | :--- |
| [`src/main/java/com/employee/directory/config/SchedulingConfig.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/SchedulingConfig.java) | Configuration bean enabling `@EnableScheduling` background tasks. | [`BackupScheduler.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/BackupScheduler.java) |
| [`src/main/java/com/employee/directory/config/BackupScheduler.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/BackupScheduler.java) | Component running `@Scheduled` tasks to invoke periodic database backup snapshots. | [`BackupService.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/BackupService.java) |
| [`src/main/java/com/employee/directory/services/BackupService.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/BackupService.java) | Service contract defining snapshot creation, file listing, and restoration operations. | [`BackupServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/BackupServiceImpl.java) |
| [`src/main/java/com/employee/directory/services/impl/BackupServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/BackupServiceImpl.java) | Implementation serializing entity state into JSON snapshots in `backups/` folder. | [`EmployeeRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/EmployeeRepository.java), [`AuditLogRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/AuditLogRepository.java) |
| [`src/main/java/com/employee/directory/controllers/BackupController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/BackupController.java) | Controller exposing `/api/v1/backups/create`, `/api/v1/backups`, and `/api/v1/backups/restore`. | [`BackupService.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/BackupService.java) |
| [`src/main/java/com/employee/directory/config/SecurityConfig.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/SecurityConfig.java) | Security rules restricting `/api/v1/backups/**` endpoints to `ROLE_ADMIN`. | [`BackupController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/BackupController.java) |
| [`src/test/java/com/employee/directory/services/BackupServiceTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/services/BackupServiceTest.java) | Unit tests verifying JSON snapshot generation and file listing logic. | [`BackupServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/BackupServiceImpl.java) |
| [`src/test/java/com/employee/directory/controllers/BackupControllerIntegrationTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/controllers/BackupControllerIntegrationTest.java) | Integration tests asserting `POST /api/v1/backups/create` authorization and HTTP status codes. | [`BackupController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/BackupController.java) |
| [`README.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/README.md) | Updated README with database backup architecture notes and API table entries. | Repository Root |
| [`CHANGELOG.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/CHANGELOG.md) | Updated technical changelog for release `[1.9.0]`. | Repository Root |
| [`BUILD_NOTES.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/BUILD_NOTES.md) | Appended Iteration 10 build log entry. | Repository Root (Ignored) |

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
   *Expected output*: `Tests run: 45, Failures: 0, Errors: 0, Skipped: 0` -> `BUILD SUCCESS`.

3. **Start the Spring Boot server**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

4. **Trigger Manual Database Snapshot as Admin**:
   First, obtain admin Bearer token via `POST /api/v1/auth/login`:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
   ```
   Then trigger backup snapshot creation:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/backups/create" \
        -H "Authorization: Bearer <your_jwt_token>"
   ```
   *Expected response*:
   ```json
   {
     "message": "Backup snapshot created successfully",
     "filename": "snapshot_20260808_145227.json"
   }
   ```

5. **List Generated Backup Snapshots**:
   ```bash
   curl -X GET "http://localhost:8080/api/v1/backups" \
        -H "Authorization: Bearer <your_jwt_token>"
   ```
   *Expected response*: List of snapshot filenames in `backups/` directory.

---

## Candidate Next Iterations

### Option 1: Soft Delete & Data Recovery Trash Bin (Recommended)
- **Plain English**: Update deletion logic to perform soft deletes (`is_deleted` flag or `deleted_at` timestamp) and expose `/api/v1/employees/trash` to view and restore deleted records.
- **Benefit**: Prevents accidental data loss and allows easy restoration of deleted personnel files.
- **Trade-off**: Modifies JPA query filters to exclude soft-deleted records.
- **Interview Answer**: *"I implemented soft deletion with a trash bin recovery endpoint to prevent permanent accidental data loss."*
- **Manual Test Steps**:
  1. Delete employee via `DELETE /api/v1/employees/1`.
  2. Restore employee via `POST /api/v1/employees/1/restore`.

### Option 2: Health Check & Actuator Monitoring Dashboard
- **Plain English**: Integrate Spring Boot Actuator (`/actuator/health`, `/actuator/metrics`) to monitor JVM memory, database connectivity, and HTTP request metrics.
- **Benefit**: Essential for DevOps monitoring and production readiness verification.
- **Trade-off**: Exposes internal runtime metrics requiring security scoping.
- **Interview Answer**: *"I integrated Spring Boot Actuator metrics and custom health indicators to monitor JVM heap and database connection pool health."*
- **Manual Test Steps**:
  1. Query `GET /actuator/health`.
  2. Inspect metrics via `GET /actuator/metrics`.

### Option 3: GraphQL Query API Interface
- **Plain English**: Add Spring GraphQL (`spring-boot-starter-graphql`) alongside REST to allow clients to request exact employee fields and relations via flexible GraphQL queries.
- **Benefit**: Reduces over-fetching and under-fetching of employee data for frontend clients.
- **Trade-off**: Requires maintaining GraphQL schema definitions (`schema.graphqls`).
- **Interview Answer**: *"I added a GraphQL query layer alongside REST to eliminate over-fetching for client applications."*
- **Manual Test Steps**:
  1. Post GraphQL query to `/graphql`.
  2. Verify JSON output matching requested fields.
