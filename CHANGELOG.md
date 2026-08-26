# Changelog

All notable changes to the Employee Directory API project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-08-26

### Added
- **GraphQL API Layer**: Integrated `spring-boot-starter-graphql` exposing GraphQL schema endpoints at `/graphql`.
- **GraphQL Schema Definition**: Created `schema.graphqls` defining queries (`employees`, `employeeById`, `departmentAnalytics`) and mutations (`createEmployee`, `deleteEmployee`).
- **Interactive GraphiQL Playground**: Enabled in-browser GraphiQL IDE at `/graphiql` for testing custom field selections.
- **GraphQL Integration Test Suite**: Added `EmployeeGraphQLControllerTest` expanding test coverage to 47 passing tests.

## [1.0.0] - 2026-08-26

### Added
- **Core Domain & JPA Persistence Layer**: Defined `Employee`, `User`, and `AuditLog` entities with Spring Data JPA repositories.
- **REST Endpoints & DTO Mappings**: Implemented paginated and sorted employee endpoints (`/api/v1/employees`), employee retrieval by ID, creation, update, and deletion.
- **Spring Security 6 & JWT Authentication**: Implemented stateless JWT Bearer token authentication, user registration, login, password hashing via BCrypt, and Role-Based Access Control (RBAC).
- **Token-Bucket Rate Limiting**: Added Bucket4j HTTP request throttling filter (50 req/min per IP).
- **Caching Layer**: Configured Caffeine In-Memory Cache with `@Cacheable` and `@CacheEvict` annotations for optimal API throughput.
- **Real-Time Event Streaming**: Created Server-Sent Events (SSE) `SseEmitter` broadcast publisher at `GET /api/v1/employees/stream`.
- **Department Analytics**: Integrated `/api/v1/employees/analytics/departments` endpoint returning headcount, average salary, minimum/maximum salary per department.
- **Bulk CSV Import & Export**: Implemented streaming CSV file uploads with validation and CSV data exports.
- **Automated Database Backups**: Built `@Scheduled` background task `BackupScheduler` for periodic JSON database state snapshots and restore capability.
- **OpenAPI 3.0 & Swagger UI**: Integrated Springdoc OpenAPI starter for interactive Swagger UI API exploration.
- **GitHub Actions CI Workflow**: Added `.github/workflows/ci.yml` pipeline executing build and test suites on GitHub.
- **Unit & Integration Test Suite**: Engineered comprehensive JUnit 5 and MockMvc test suite with 100% endpoint verification.
