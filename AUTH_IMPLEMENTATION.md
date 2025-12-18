# SIMS - Student Internship Management System
## Authentication & Authorization Implementation Guide

## System Overview

SIMS is a web-based Student Internship Management System with JWT-based authentication supporting three user roles:
- **USER** (Students) - Apply to internships posted by companies
- **COMPANY** - Post internship positions and review applications
- **ADMIN** - System administrators managing internships and users

## Features Implemented

✅ User registration and login with role selection
✅ JWT token generation and validation (24-hour expiration)
✅ BCrypt password encryption
✅ Cookie-based JWT storage (httpOnly for security)
✅ Spring Security integration with authentication filters
✅ Role-based dashboard routing
✅ Responsive Tailwind CSS UI
✅ Database migrations with Flyway
✅ Student profiles and company profiles auto-creation
✅ REST API endpoints for authentication

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) DEFAULT 'USER' NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Student Profiles (auto-created for USER role)
```sql
CREATE TABLE student_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    major VARCHAR(100),
    year INT,
    cv_file VARCHAR(255)
);
```

### Companies (auto-created for COMPANY role)
```sql
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20)
);
```

### Internships (posted by companies)
```sql
CREATE TABLE internships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(100),
    seats INT NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);
```

### Applications (student applications)
```sql
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, internship_id),
    FOREIGN KEY (student_id) REFERENCES student_profiles(id),
    FOREIGN KEY (internship_id) REFERENCES internships(id)
);
```

## Core Components

### 1. Entity Classes

**UserEntity** - Core user with email, username, password, role, active status
**StudentProfileEntity** - Extended student data (auto-created on USER registration)
**CompanyEntity** - Company information (auto-created on COMPANY registration)
**InternshipEntity** - Internship positions posted by companies
**ApplicationEntity** - Student applications with PENDING/APPROVED/REJECTED status

### 2. Security Components

**JwtTokenProvider** - Token generation, validation, and claim extraction
- Generates 24-hour JWT tokens
- Validates token integrity and expiration
- Extracts email claims from tokens

**JwtAuthenticationFilter** - Intercepts requests and validates JWT
- Checks Authorization header and httpOnly cookies
- Creates Spring Security Authentication objects
- Sets userEmail in request context

**SecurityConfig** - Spring Security configuration
- Disables CSRF for API endpoints
- Permits public access to `/`, `/auth/**`, static resources
- Requires authentication for `/dashboard` and protected routes
- BCrypt password encoder bean

### 3. Service Layer

**AuthService** - Handles user registration and authentication
```java
register(email, username, password, role)  // Creates user + profile
login(email, password)                     // Returns JWT token
getUserByEmail(email)                      // Retrieves user data
```

### 4. Controllers

**AuthController** - Authentication endpoints
```
GET  /auth/login              // Login form
POST /auth/login              // Process login, set JWT cookie
GET  /auth/register           // Registration form
POST /auth/register           // Create account
GET  /auth/logout             // Clear JWT cookie
POST /api/auth/login          // REST login endpoint
POST /api/auth/register       // REST registration endpoint
```

**HomeController** - Dashboard routing
```
GET  /                        // Landing page (index.html)
GET  /dashboard               // Route to role-specific dashboard
                              // USER  → user-dashboard.html
                              // COMPANY → company-dashboard.html
                              // ADMIN → admin-dashboard.html
```

## Application Flow

### Registration Flow
1. User navigates to `/auth/register`
2. Selects role (Student/Company)
3. Submits email, username, password
4. AuthService creates:
   - UserEntity (with role and encrypted password)
   - StudentProfileEntity (if USER role)
   - CompanyEntity (if COMPANY role)
5. User redirected to login page

### Login Flow
1. User navigates to `/auth/login`
2. Enters email and password
3. AuthService validates credentials
4. JWT token generated
5. Token stored in httpOnly cookie
6. User redirected to `/dashboard`

### Dashboard Routing
1. JwtAuthenticationFilter validates JWT from cookie
2. Creates Spring Security Authentication
3. HomeController checks user role
4. Routes to appropriate dashboard:
   - USER → Student dashboard (browse/apply to internships)
   - COMPANY → Company dashboard (post/manage internships)
   - ADMIN → Admin dashboard (manage users and system)

## Frontend Pages (Tailwind CSS)

**index.html** - Landing page with features and CTA buttons
**auth/login.html** - Login form with email/password fields
**auth/register.html** - Registration with role selector (Student/Company)
**dashboard/user-dashboard.html** - Student dashboard with stats and applications
**dashboard/company-dashboard.html** - Company dashboard with job postings
**dashboard/admin-dashboard.html** - Admin panel with system controls

## Configuration

### Database Setup
```bash
export DB_NAME=sims_db
export DB_USERNAME=root
export DB_PASSWORD=NewStrongPass123!
```

### JWT Configuration (application.properties)
```properties
jwt.secret=${JWT_SECRET:defaultSecretKeyForDevelopmentPurposesOnly12345678901234567890}
jwt.expiration=${JWT_EXPIRATION:86400000}
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

### Run Application
```bash
./gradlew bootRun
# or
java -jar build/libs/sims-0.0.1-SNAPSHOT.jar
```

## Usage Examples

### Register as Student
```bash
POST /auth/register
email=student@example.com
username=johndoe
password=Pass123!
role=USER
```

### Register as Company
```bash
POST /auth/register
email=company@example.com
username=techcorp
password=Pass123!
role=COMPANY
```

### Login
```bash
POST /auth/login
email=student@example.com
password=Pass123!

# Response: Sets JWT in httpOnly cookie
# Redirects to /dashboard (student-dashboard.html)
```

### API Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=student@example.com&password=Pass123!"

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "johndoe",
  "role": "USER",
  "message": "Login successful"
}
```

## Security Features

1. **Password Hashing** - BCrypt with random salt
2. **JWT Signing** - HMAC-SHA256 cryptographic signature
3. **HttpOnly Cookies** - Prevents XSS attacks
4. **Token Expiration** - 24-hour validity period
5. **Spring Security** - Proper authentication context and authorization
6. **CSRF Protection** - Disabled for stateless API (enabled for forms if needed)
7. **Role-Based Access** - Different dashboards per role

## File Structure

```
src/main/java/com/example/sims/
├── entity/
│   ├── UserEntity.java
│   ├── StudentProfileEntity.java
│   ├── CompanyEntity.java
│   ├── InternshipEntity.java
│   └── ApplicationEntity.java
├── security/
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
├── config/
│   └── SecurityConfig.java
├── service/
│   └── AuthService.java
├── controller/
│   ├── AuthController.java
│   └── HomeController.java
├── repo/
│   ├── UserRepository.java
│   ├── StudentProfileRepository.java
│   ├── CompanyRepository.java
│   └── etc.
└── SimsApplication.java

src/main/resources/
├── db/migrations/
│   ├── V1__sims_schema.sql
│   └── V2__update_users_for_auth.sql
├── templates/
│   ├── index.html
│   ├── auth/
│   │   ├── login.html
│   │   └── register.html
│   └── dashboard/
│       ├── user-dashboard.html
│       ├── company-dashboard.html
│       └── admin-dashboard.html
└── application.properties
```

## Troubleshooting

**403 Forbidden on /dashboard**
- Ensure you logged in (JWT cookie set)
- Check browser cookies for "jwt" cookie
- Verify JWT token is valid and not expired

**User not found on login**
- Register first via /auth/register
- Verify email matches during login

**Authentication filter not working**
- Ensure JwtAuthenticationFilter is registered in SecurityConfig
- Check that SecurityContextHolder is being set properly
- Verify JWT secret is consistent

**Database migration fails**
- Drop and recreate database
- Check Flyway migration files syntax
- Ensure MySQL user has proper permissions

## Testing Workflow

1. Start application: `./gradlew bootRun`
2. Navigate to http://localhost:8080
3. Click "Sign Up"
4. Register as Student with email, username, password
5. Login with same credentials
6. Should redirect to Student Dashboard
7. Logout clears JWT cookie

## Future Enhancements

- Refresh token implementation
- Email verification on registration
- Password reset functionality
- Two-factor authentication
- OAuth2 social login integration
- Role-based endpoint protection with @PreAuthorize
- Audit logging for security events
