# Individual Report: Sok Sana

**Project:** Student Internship Management System (SIMS)
**Date:** December 23, 2025
**Role:** Backend developer — implemented controller, DTO, entity, repository, and service layers

---

## Summary

I contributed to the SIMS backend by designing and implementing core Spring components for authentication and user management. My work focused on creating and wiring the Controller, DTOs, Entity models, Repository interfaces, and Service classes that implement business logic and data access. These changes enable secure login/register flows, user profile handling, and CRUD operations for internships and applications.

---

## Key Responsibilities

- **Controller:** Implemented REST and MVC endpoints (e.g., `AuthController`, `HomeController`) to handle HTTP requests, validate input, and return appropriate views or JSON responses.
- **DTO (Data Transfer Objects):** Designed DTOs such as `AuthResponse` and `UserDTO` to encapsulate payloads between client and server, and to decouple API contracts from persistence models.
- **Entity:** Defined JPA entities (e.g., `UserEntity`, `StudentProfileEntity`, `InternshipEntity`, `CompanyEntity`, `ApplicationEntity`) with proper mappings, constraints, and lifecycle annotations.
- **Repository:** Created Spring Data JPA repositories to provide typed CRUD operations and custom queries for user lookups, application retrieval, and internship listings.
- **Service:** Implemented service-layer classes to host business rules, transaction boundaries, authentication logic, password hashing, and interactions between repositories and controllers.

---

## Challenges

- **Schema changes and migration:** Updating user-related entities required careful migration scripts and backward compatibility consideration to avoid data loss.
- **DTO ↔ Entity mapping:** Keeping DTOs concise while preserving required fields for security and display required explicit mapping and validation logic.
- **Authentication flow:** Integrating password hashing, token/session handling, and error handling across controller and service layers introduced edge cases to test (failed login, duplicate registration, role handling).
- **Template integration:** Ensuring server-side controllers and templates (login/register/dashboard) remain consistent after backend changes required coordination with frontend templates.

---

## Tools and Technologies Used

- **Language & Framework:** Java, Spring Boot (Spring MVC, Spring Data JPA, Spring Security components used or prepared for integration)
- **Database & Migration:** H2/Postgres-compatible SQL for development; Flyway-style SQL migrations (`src/main/resources/db/migrations/V2__update_users_for_auth.sql`)
- **Build & Dependency:** Gradle (`gradlew`)
- **IDE & Debugging:** IntelliJ IDEA or VS Code, browser devtools for template testing
- **Version Control:** Git (branching, commits for feature work)

---

## Notes / Next Steps

- Run backend unit/integration tests and a local migration on a staging DB before deploying migrations.
- Add mapping tests for DTO ↔ Entity conversions and authentication edge cases.
- If desired, I can add a short changelog of commits touching auth-related files or include per-file summaries.

