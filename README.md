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
DB_NAME=sims_lead
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
  "INSERT INTO users (name, email, role) VALUES ('Alice', 'alice@example.com', 'STUDENT');"
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
| **Backend** | Spring Boot 3, Spring Data JPA, Hibernate |
| **Database** | MySQL 8.x, Flyway migrations |
| **Build** | Gradle 8+, Java 21 |
| **Config** | `.env` support via `java-dotenv` |

### Data Model

The project manages five core entities (built incrementally via migrations `V1`–`V6`):

1. **Users** — Base entity for all system users (students, admins).
   - Fields: `id`, `name`, `email`, `role`, `major`, `graduation_year`
   - Repository: [UserRepository](src/main/java/com/example/sims/repository/UserRepository.java)
   - Service: [UserService](src/main/java/com/example/sims/service/UserService.java)

2. **Companies** — Organizations offering internships.
   - Migration: `V2__company_schema.sql`

3. **Internships** — Internship programs under companies.
   - Migration: `V3__internship_schema.sql`

4. **Applications** — Student applications for internships.
   - Migration: `V4__application_schema.sql`

5. **Placements** — Outcomes of internship placements.
   - Migration: `V5__placement_schema.sql`

6. **Evaluations** — Performance evaluations for placed interns.
   - Migration: `V6__evaluation_schema.sql`

### Request Flow

```
HTTP Request
    ↓
Controller (HomeController, UserController)
    ↓
Service (UserService)
    ↓
Repository (UserRepository → JPA)
    ↓
Hibernate/JPA Entity (User.java)
    ↓
MySQL Database
```

**Example:** `GET /users`
1. `UserController.listUsers()` receives request.
2. Calls `userRepository.findAll()`.
3. JPA/Hibernate queries MySQL for all users.
4. Results bound to Thymeleaf model.
5. [users.html](src/main/resources/templates/clients/user.html) renders the list.

## What's in Each Migration

| File | Purpose |
|------|---------|
| `V1__init_schema.sql` | Creates foundational tables (users, roles) |
| `V2__company_schema.sql` | Adds company entities and relationships |
| `V3__internship_schema.sql` | Defines internship programs per company |
| `V4__application_schema.sql` | Tracks student applications to internships |
| `V5__placement_schema.sql` | Records successful placements |
| `V6__evaluation_schema.sql` | Stores performance evaluations |

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
