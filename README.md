# SIMS — Student Internship Management System

**A Spring Boot 3 (Java 21) web application** for managing student internships, company placements, and internship applications. Built with Thymeleaf for server-rendered views, Spring Data JPA for persistence, Flyway for schema versioning, and MySQL as the database.

### Key Features

- **Student & User Management** — Create and manage student profiles with role-based access.
- **Company & Internship Data** — Track companies and their internship programs.
- **Application Tracking** — Handle student applications for internships.
- **Placement Records** — Log placement outcomes and evaluations.
- **Incremental Schema** — All data structures versioned via Flyway migrations.
- **Environment Configuration** — Externalized DB credentials via `.env` file support.

## Quick Start

- Requirements: Java 21, MySQL 8.x, macOS/Linux/Windows
- Repo already includes Gradle wrapper (`./gradlew`) so Gradle install is optional.

```bash
# 1) Copy sample env and adjust values
cp .env.example .env  # if you have one; otherwise create .env (see below)

# 2) Create the database in MySQL (adjust name if needed)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sims_lead CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

# 3) Run the app (Flyway runs migrations automatically)
./gradlew bootRun
```

Then open http://localhost:8080

- Home page: /
- Users page: /users

## Configuration

Configuration is managed via Spring `application.properties` and a `.env` file. The project loads entries from `.env` at startup (see `DotenvConfig`).

- App properties: [src/main/resources/application.properties](src/main/resources/application.properties)
- Dotenv loader: [src/main/java/com/example/sims/config/DotenvConfig.java](src/main/java/com/example/sims/config/DotenvConfig.java)

Supported env vars (with defaults shown in `application.properties`):

- `DB_NAME` (default: `sims_****`)
- `DB_USERNAME` (default: `****`)
- `DB_PASSWORD` (default: `**********`)

Example `.env` (create at the project root):

```
DB_NAME=sims_db
DB_USERNAME=root
DB_PASSWORD=yourStrongPassword
```

## Database & Migrations

- Migrations live in: [src/main/resources/db/migrations](src/main/resources/db/migrations)
- They run automatically on startup via Flyway.
- JPA DDL mode is `validate` to ensure schema matches entities.

Add a new migration:

1. Create a new file under `src/main/resources/db/migrations` following Flyway naming, e.g. `V7__add_index_to_users_email.sql`.
2. Start the app; Flyway will apply it.

Tip: Keep migration version numbers incremental and the description lowercase with underscores.

### Data Seeding (No Migration Needed)

For inserting or updating data (DML) you can run SQL directly against the database; no Flyway migration is required. Reserve Flyway migrations for schema changes (DDL).

Example (macOS):

```bash
# Insert a demo user directly into MySQL
mysql -u "$DB_USERNAME" -p -D "$DB_NAME" -e \
  "INSERT INTO users (name, email, role) VALUES ('saturo', 'saturo@example.com', 'STUDENT');"
```

Notes:
- Ensure the table exists (the app runs Flyway migrations first, then validates schema).
- Do not modify existing applied migration files; add new ones only for schema changes.

## Build, Run, Test

```bash
# Run the app in dev mode
./gradlew bootRun

# Build a runnable jar
./gradlew build
java -jar build/libs/sims-0.0.1-SNAPSHOT.jar

# Run tests
./gradlew test

# Open test report (HTML)
open build/reports/tests/test/index.html  # macOS
```

## Project Architecture

### Tech Stack

| Layer | Tech |
|-------|------|
| **Frontend** | Thymeleaf, HTML5 |
| **Backend** | Spring Boot 3, Spring Data JPA, Hibernate, Spring Security |
| **Authentication** | JWT (JSON Web Tokens) with jjwt library |
| **Database** | MySQL 8.x, Flyway migrations |
| **Build** | Gradle 8+, Java 21 |
| **Config** | `.env` support via `java-dotenv` |
| **File Upload** | Apache Commons IO, multipart support (max 10MB) |

### Data Model

The project manages core entities built in V1 migration with authentication updates in V2:

1. **Users** — Base entity for all system users (students, admins, companies).
   - Fields: `id`, `email`, `username`, `password`, `role`, `active`, `created_at`
   - Roles: `USER` (students), `ADMIN`, `COMPANY`
   - Repository: [UserRepository](src/main/java/com/example/sims/repo/UserRepository.java)
   - Service: [AuthService](src/main/java/com/example/sims/service/AuthService.java)

2. **Student Profiles** — Detailed student information linked to users.
   - Fields: `id`, `user_id`, `student_code`, `full_name`, `major`, `year`, `cv_file`

3. **Companies** — Organizations offering internships.
   - Fields: `id`, `user_id`, `company_name`, `address`, `contact_email`, `contact_phone`
   - Linked to Users for authentication

4. **Internships** — Internship programs offered by companies.
   - Fields: `id`, `company_id`, `title`, `description`, `location`, `seats`, `start_date`, `end_date`, `created_at`

5. **Applications** — Student applications for internships.
   - Fields: `id`, `student_id`, `internship_id`, `status` (PENDING/APPROVED/REJECTED), `applied_at`

6. **Placements** — Outcomes of approved internship applications.
   - Fields: `id`, `application_id`, `admin_id`, `placement_date`, `status` (PLACED/CANCELLED)

7. **Evaluations** — Performance evaluations for placed interns.
   - Fields: `id`, `placement_id`, `evaluator_id`, `score` (1-100), `remarks`, `evaluated_at`

8. **Login Attempts** — Audit trail for authentication security.
   - Fields: `id`, `user_id`, `attempt_time`, `success`

### Request Flow

```
HTTP Request
    ↓
Controller (AuthController, HomeController)
    ↓
Service (AuthService, business logic)
    ↓
Repository (JPA interfaces)
    ↓
Hibernate/JPA Entity (User.java, StudentProfile.java, etc.)
    ↓
MySQL Database
```

**Example:** `POST /auth/login`
1. `AuthController.login()` receives credentials.
2. Calls `authService.authenticate()` for validation.
3. Logs login attempt to `login_attempts` table.
4. Returns JWT token on success or error message on failure.

## Security & Authentication

The project implements role-based access control (RBAC) with JWT token authentication:

- **Roles**: `USER` (students), `ADMIN`, `COMPANY`
- **Authentication**: Spring Security with JWT tokens (jjwt library)
- **Password**: Stored securely; authentication managed by Spring Security
- **Login Tracking**: All login attempts logged to `login_attempts` table for audit trails
- **Configuration**: See [SecurityConfig](src/main/java/com/example/sims/config/SecurityConfig.java)

The `AuthService` handles:
- User authentication and validation
- JWT token generation and validation
- Login attempt recording

## What's in Each Migration

| File | Purpose |
|------|---------|
| `V1__sims_schema.sql` | Creates all foundational tables (users, student_profiles, companies, internships, applications, placements, evaluations, login_attempts) |
| `V2__update_users_for_auth.sql` | Updates users table for authentication (adds username, changes enabled to active, updates role handling) |

When the app starts, **Flyway automatically applies all pending migrations** in order, ensuring the schema is always in sync.

## Common Issues

- Database connection error
  - Confirm MySQL running and credentials in `.env` match.
  - Check URL assembled from `DB_NAME`, default is `jdbc:mysql://localhost:3306/sims_lead`.

- Flyway validation error
  - Ensure the database is clean for new setups, or that all migration versions are present. Avoid editing applied migration files; create a new one instead.

- Port 8080 already in use
  - Stop the other process or run with `--args='--server.port=8081'`.

- Hibernate `validate` failures
  - If schema is behind, add a proper migration instead of switching to `update`.

## Contributing

- **Branching:** Use feature branches (e.g., `feature/add-company-api`, `fix/user-validation`).
- **Migrations:** Always create new migration files; never edit applied ones. Follow `V<N>__description.sql` naming.
- **Architecture:** Keep the three-layer pattern:
  - **Controller** → HTTP entry point, delegate to service.
  - **Service** → Business logic, transactions, validation.
  - **Repository** → Data access only, use Spring Data queries.
- **Entities:** Use JPA annotations; add constraints and validations.
- **Pull Requests:** Include a description of what was added/fixed and which tables/entities are affected.

## Development Tips

- **Hot Reload:** Dev mode watches for changes. Stop and restart `./gradlew bootRun` to reload.
- **SQL Debugging:** Enable `spring.jpa.show-sql=true` in `application.properties` to see generated SQL.
- **Custom Queries:** Use `@Query` annotation in repositories for complex logic instead of multiple JPA method names.
- **Testing:** Add unit tests in `src/test/java/com/example/sims/` and run with `./gradlew test`.

## License

Internal project. If you need a license, propose one in a PR.
