# SIMS — Spring Boot App

A simple Spring Boot 3 (Java 21) application using Thymeleaf for server‑rendered views, JPA/Hibernate for data access, Flyway for DB migrations, and MySQL as the database. Includes Gradle wrapper for build/run tasks and `.env` support for configuration.

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

## Project Structure

- App entry: [src/main/java/com/example/sims/SimsApplication.java](src/main/java/com/example/sims/SimsApplication.java)
- MVC controllers:
  - [src/main/java/com/example/sims/controller/HomeController.java](src/main/java/com/example/sims/controller/HomeController.java) → `GET /`
  - [src/main/java/com/example/sims/controller/UserController.java](src/main/java/com/example/sims/controller/UserController.java) → `GET /users`
- Views (Thymeleaf):
  - [src/main/resources/templates/index.html](src/main/resources/templates/index.html)
  - [src/main/resources/templates/clients/user.html](src/main/resources/templates/clients/user.html)
- Entity/Repository/Service:
  - [src/main/java/com/example/sims/entity/User.java](src/main/java/com/example/sims/entity/User.java)
  - [src/main/java/com/example/sims/repository/UserRepository.java](src/main/java/com/example/sims/repository/UserRepository.java)
  - [src/main/java/com/example/sims/service/UserService.java](src/main/java/com/example/sims/service/UserService.java)

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

- Use feature branches and open PRs with a brief description.
- Keep migrations small and reversible; follow the `V<N>__description.sql` pattern.
- Favor service + repository patterns and keep controllers thin.

## License

Internal project. If you need a license, propose one in a PR.
