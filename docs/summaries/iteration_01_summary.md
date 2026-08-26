# Iteration 1 Summary: Core Spring Boot Employee Directory REST API

## Plain English Summary
In Iteration 1, we built a production-ready RESTful **Employee Directory API** using **Spring Boot 3.3.0** and **Java 17**. The service handles full CRUD (Create, Read, Update, Delete) operations on employee records stored in an in-memory **H2 database**. It supports multi-field pagination, dynamic sorting, department filtering, and keyword search across names and email addresses. The API enforces strict Bean Validation constraints on incoming requests and features a global exception handling architecture that maps domain errors to RFC-7807-style JSON error payloads.

---

## File Map & Connections

| File Path | Description | Connects To |
| :--- | :--- | :--- |
| [`pom.xml`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/pom.xml) | Maven build configuration defining dependencies for Spring Web, Data JPA, Validation, H2, and Test. | Java 17, Spring Boot 3.3.0 |
| [`src/main/resources/application.yml`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/resources/application.yml) | Application runtime properties configuring H2 database, web console, server port 8080, and JPA logging. | Spring Boot Runtime, H2 Engine |
| [`src/main/java/com/employee/directory/EmployeeDirectoryApplication.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/EmployeeDirectoryApplication.java) | Main entry point class bootstrapping the Spring application context. | Spring Framework |
| [`src/main/java/com/employee/directory/enums/EmployeeStatus.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/enums/EmployeeStatus.java) | Enum representing employment statuses (`ACTIVE`, `ON_LEAVE`, `TERMINATED`). | `Employee.java`, `EmployeeDTO.java` |
| [`src/main/java/com/employee/directory/models/Employee.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/models/Employee.java) | JPA entity mapping employee data with automated `@PrePersist` and `@PreUpdate` timestamp management. | `EmployeeRepository.java` |
| [`src/main/java/com/employee/directory/dto/EmployeeDTO.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/dto/EmployeeDTO.java) | Data Transfer Object carrying validated payload constraints (`@NotBlank`, `@Email`, `@PastOrPresent`). | `EmployeeController.java`, `EmployeeService.java` |
| [`src/main/java/com/employee/directory/dto/PagedResponseDTO.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/dto/PagedResponseDTO.java) | Generic container DTO for paginated query results with pagination metadata. | `EmployeeController.java` |
| [`src/main/java/com/employee/directory/dto/ApiErrorResponse.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/dto/ApiErrorResponse.java) | Standardized JSON structure for API error responses. | `GlobalExceptionHandler.java` |
| [`src/main/java/com/employee/directory/exceptions/ResourceNotFoundException.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/exceptions/ResourceNotFoundException.java) | Domain exception thrown when an employee record is not found. | `GlobalExceptionHandler.java` |
| [`src/main/java/com/employee/directory/exceptions/DuplicateResourceException.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/exceptions/DuplicateResourceException.java) | Domain exception thrown when unique constraints (e.g., email) are violated. | `GlobalExceptionHandler.java` |
| [`src/main/java/com/employee/directory/exceptions/GlobalExceptionHandler.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/exceptions/GlobalExceptionHandler.java) | `@RestControllerAdvice` mapping exceptions to HTTP 404, 409, 400, and 500 status codes. | `EmployeeController.java` |
| [`src/main/java/com/employee/directory/repositories/EmployeeRepository.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/repositories/EmployeeRepository.java) | Spring Data repository supporting JPA Specification queries for dynamic search and filtering. | `EmployeeServiceImpl.java` |
| [`src/main/java/com/employee/directory/services/EmployeeService.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/EmployeeService.java) | Interface defining employee business logic contracts. | `EmployeeController.java` |
| [`src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/services/impl/EmployeeServiceImpl.java) | Service implementation containing business validation, JPA Specification building, and DTO conversion. | `EmployeeRepository.java` |
| [`src/main/java/com/employee/directory/controllers/EmployeeController.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/controllers/EmployeeController.java) | REST controller publishing `/api/v1/employees` endpoints. | `EmployeeService.java` |
| [`src/main/java/com/employee/directory/config/DataLoader.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/main/java/com/employee/directory/config/DataLoader.java) | Startup data populator seeding initial sample employee records into H2. | `EmployeeRepository.java` |
| [`src/test/java/com/employee/directory/services/EmployeeServiceTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/services/EmployeeServiceTest.java) | Unit tests verifying business logic using JUnit 5 and Mockito. | `EmployeeServiceImpl.java` |
| [`src/test/java/com/employee/directory/controllers/EmployeeControllerIntegrationTest.java`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/src/test/java/com/employee/directory/controllers/EmployeeControllerIntegrationTest.java) | Integration tests verifying REST endpoints using `@SpringBootTest` and `MockMvc`. | `EmployeeController.java` |
| [`README.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/README.md) | Project documentation containing setup, architecture notes, endpoints, and running guide. | Repository Root |
| [`CHANGELOG.md`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/CHANGELOG.md) | Technical changelog tracking project releases. | Repository Root |
| [`LICENSE`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/LICENSE) | Standard MIT License. | Repository Root |
| [`.gitignore`](file:///C:/Users/marve/Desktop/AI-286-Builds/Build_71/.gitignore) | Git ignore configuration excluding target build artifacts, secrets, and internal notes logs. | Git Version Control |

---

## Manual Testing Steps (In Another Terminal Window)

To test the pushed code locally in another terminal window:

1. **Open a new terminal window** and navigate to the project directory:
   ```bash
   cd C:\Users\marve\Desktop\AI-286-Builds\Build_71
   ```

2. **Run all unit & integration tests**:
   ```bash
   .\mvnw.cmd test
   ```
   *Expected output*: `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0` -> `BUILD SUCCESS`.

3. **Start the Spring Boot server**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
   *Expected output*: Tomcat starts on port 8080.

4. **In a second terminal window (or PowerShell), execute curl / API requests**:

   - **Get All Seeded Employees (Paginated & Sorted)**:
     ```powershell
     Invoke-RestMethod -Uri "http://localhost:8080/api/v1/employees?page=0&size=5&sort=lastName,asc" -Method Get | ConvertTo-Json -Depth 5
     ```

   - **Filter by Department & Search Query**:
     ```powershell
     Invoke-RestMethod -Uri "http://localhost:8080/api/v1/employees?department=Engineering&search=Alice" -Method Get | ConvertTo-Json -Depth 5
     ```

   - **Create a New Employee**:
     ```powershell
     $body = @{
         firstName = "Sarah"
         lastName = "Connor"
         email = "sarah.connor@company.com"
         department = "Security"
         jobTitle = "Security Specialist"
         salary = 98000.00
         hireDate = "2024-03-10"
         status = "ACTIVE"
     } | ConvertTo-Json

     Invoke-RestMethod -Uri "http://localhost:8080/api/v1/employees" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
     ```

   - **Update an Employee**:
     ```powershell
     $updateBody = @{
         firstName = "Sarah"
         lastName = "Connor-Reese"
         email = "sarah.connor@company.com"
         department = "Security"
         jobTitle = "Chief Security Officer"
         salary = 135000.00
         hireDate = "2024-03-10"
         status = "ACTIVE"
     } | ConvertTo-Json

     Invoke-RestMethod -Uri "http://localhost:8080/api/v1/employees/11" -Method Put -Body $updateBody -ContentType "application/json" | ConvertTo-Json
     ```

   - **Delete an Employee**:
     ```powershell
     Invoke-RestMethod -Uri "http://localhost:8080/api/v1/employees/11" -Method Delete
     ```

   - **Verify H2 Web Console**:
     Open browser to `http://localhost:8080/h2-console`, connect with JDBC URL `jdbc:h2:mem:employeedb`, User `sa`, Password `password`.

---

## Candidate Next Iterations

### Option 1: OpenAPI / Swagger Interactive Documentation (Recommended)
- **Plain English**: Integrate Springdoc OpenAPI to generate interactive Swagger UI documentation at `/swagger-ui.html` and OpenAPI 3.0 spec JSON at `/v3/api-docs`.
- **Benefit**: Allows frontend developers and external integrators to test endpoints interactively in the browser without writing manual curl or Postman requests.
- **Trade-off**: Slightly increases binary size and exposes API endpoints unless restricted by security rules in production.
- **Interview Answer**: *"I added OpenAPI/Swagger documentation to provide interactive, self-documenting API endpoints. This accelerates frontend integration and establishes clean, contract-first communication across engineering teams."*
- **Manual Test Steps**:
  1. Start server with `.\mvnw.cmd spring-boot:run`.
  2. Open browser to `http://localhost:8080/swagger-ui.html`.
  3. Execute GET `/api/v1/employees` directly from the interactive Swagger page.

### Option 2: Department Metrics & Salary Analytics Aggregation Endpoint
- **Plain English**: Add analytics endpoints (`GET /api/v1/employees/analytics/departments`) returning headcount, average salary, minimum/maximum compensation, and status breakdown grouped by department.
- **Benefit**: Provides executive dashboards with aggregate workforce metrics without pulling full employee lists into client memory.
- **Trade-off**: Requires custom JPQL aggregate functions (`AVG`, `SUM`, `COUNT`) or native SQL projection interfaces.
- **Interview Answer**: *"I implemented aggregate metrics endpoints using JPQL projections to compute departmental headcount and salary statistics on the database engine, avoiding expensive N+1 memory loading on the application tier."*
- **Manual Test Steps**:
  1. Call `Invoke-RestMethod -Uri "http://localhost:8080/api/v1/employees/analytics/departments"`.
  2. Verify JSON payload displays headcount and average salary per department.

### Option 3: Bulk CSV Import & Export Capabilities
- **Plain English**: Implement CSV file upload endpoint (`POST /api/v1/employees/upload`) to batch-import employees and an export endpoint (`GET /api/v1/employees/export`) to stream employee directory records as a CSV file download.
- **Benefit**: Essential for HR teams needing to perform bulk data ingestion from legacy systems or export directory data for reporting.
- **Trade-off**: Requires CSV parsing, partial failure handling per row, and explicit file validation.
- **Interview Answer**: *"I added bulk CSV processing with transactional per-row error collection to support legacy HR system migration while preventing mid-batch corruption."*
- **Manual Test Steps**:
  1. POST a sample `.csv` file to `/api/v1/employees/upload`.
  2. Check response payload for success count and detailed row validation warnings.

### Option 4: Spring Security & JWT Authentication
- **Plain English**: Add Spring Security 6 with JWT token authentication, login endpoint (`/api/v1/auth/login`), and Role-Based Access Control (`ROLE_ADMIN` for writes/deletes, `ROLE_USER` for read-only).
- **Benefit**: Secures sensitive personnel and compensation data behind enterprise-grade authentication and authorization controls.
- **Trade-off**: Adds complexity to integration tests requiring mock security contexts (`@WithMockUser`) and token headers.
- **Interview Answer**: *"I integrated Spring Security with stateless JWT authorization and RBAC to protect employee PII and compensation data, restricting administrative write operations to verified admin roles."*
- **Manual Test Steps**:
  1. POST credentials to `/api/v1/auth/login` to obtain JWT token.
  2. Pass `Authorization: Bearer <token>` header in GET request.
