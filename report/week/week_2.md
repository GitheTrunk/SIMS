# Report week_2

**Project:** Student Internship Management System (SIMS)  
**Date:** December 23, 2025  
**Role:** Backend Developer

---

## Executive Summary

This report documents my contributions to the SIMS project, focusing on implementing the core home and dashboard routing functionality with role-based access control. My primary responsibility was developing the `HomeController` which manages user navigation, authentication state checking, and role-based dashboard routing.

---

## Tasks Completed

### 1. Home Controller Implementation

**File:** [`src/main/java/com/example/sims/controller/HomeController.java`](../src/main/java/com/example/sims/controller/HomeController.java)

**Description:**  
Implemented the main entry point controller for the application, managing the home page access and dashboard routing based on user authentication state and roles.

**Key Features:**
- **Home Page Routing (`/`)**: Checks if user is authenticated and redirects to dashboard if logged in, otherwise displays the landing page
- **Dashboard Routing (`/dashboard`)**: Implements role-based access control to route users to appropriate dashboards
- **Authentication State Management**: Integrates with JWT authentication filter to validate user sessions

**Code Highlights:**

```java
@GetMapping("/")
public String home(HttpServletRequest request, Model model) {
    String userEmail = (String) request.getAttribute("userEmail");
    
    if (userEmail != null) {
        return "redirect:/dashboard";
    }
    
    return "index";
}
```

This method prevents authenticated users from seeing the public landing page and automatically redirects them to their personalized dashboard.

```java
@GetMapping("/dashboard")
public String dashboard(HttpServletRequest request, Model model) {
    String userEmail = (String) request.getAttribute("userEmail");
    
    if (userEmail == null) {
        return "redirect:/auth/login";
    }
    
    UserEntity user = authService.getUserByEmail(userEmail);
    if (user == null) {
        return "redirect:/auth/login";
    }
    
    model.addAttribute("user", user);
    model.addAttribute("username", user.getUsername());
    model.addAttribute("role", user.getRole());
    
    // Route to role-specific dashboard
    if ("ADMIN".equals(user.getRole())) {
        return "dashboard/admin-dashboard";
    }
    
    // Default to user (student) dashboard
    return "dashboard/user-dashboard";
}
```

This method implements comprehensive role-based routing:
- Validates authentication state
- Retrieves user information from the database
- Adds user data to the model for view rendering
- Routes to appropriate dashboard based on user role (ADMIN → admin dashboard, USER/STUDENT → student dashboard)

---

## Technical Implementation Details

### Authentication Integration

The HomeController integrates seamlessly with the application's JWT-based authentication system:

1. **JWT Filter Integration**: The controller relies on `JwtAuthenticationFilter` which processes JWT tokens and sets the `userEmail` attribute in the request
2. **AuthService Dependency**: Injects `AuthService` to retrieve user details from the database based on email
3. **Security Configuration**: Works with `SecurityConfig` which defines that the `/dashboard` endpoint requires authentication

### Request Flow

```
User Request → JwtAuthenticationFilter → HomeController
                    ↓                           ↓
            Extract & Validate JWT      Check userEmail attribute
                    ↓                           ↓
            Set userEmail attribute     Query database for user
                                               ↓
                                    Route to appropriate dashboard
```

### Role-Based Access Control (RBAC)

Implemented a simple but effective RBAC system:
- **ADMIN role**: Routes to `dashboard/admin-dashboard.html` with admin management features
- **USER/STUDENT role**: Routes to `dashboard/user-dashboard.html` with student-specific features
- **Future extensibility**: Architecture supports easy addition of new roles (e.g., COMPANY)

---

## Integration with Other Components

### 1. AuthService Integration
- **Purpose**: Retrieve user details by email
- **Method Used**: `getUserByEmail(String email)`
- **Data Retrieved**: Complete user entity including username, role, and other profile information

### 2. Thymeleaf View Integration
The controller populates the model with necessary data for view rendering:
- `user`: Complete user entity object
- `username`: Displayed in navigation bar and dashboard header
- `role`: Used for conditional rendering in templates

### 3. Template Files Supported
- **Landing Page**: `templates/index.html`
- **Admin Dashboard**: `templates/dashboard/admin-dashboard.html`
- **Student Dashboard**: `templates/dashboard/user-dashboard.html`

---

## Security Considerations

### Authentication Validation
- **Double-check pattern**: Validates both request attribute and database user existence
- **Fail-safe redirects**: Any authentication failure redirects to login page
- **No sensitive data exposure**: Only necessary user information is passed to views

### Session Management
- Relies on JWT token stored in HTTP-only cookies
- No session data stored on server side
- Stateless authentication approach

---

## Testing Scenarios

### 1. Unauthenticated User Access
- **Test**: Access `/` without JWT token
- **Expected**: Display landing page
- **Result**: ✅ Pass

### 2. Authenticated User Accessing Home
- **Test**: Access `/` with valid JWT token
- **Expected**: Redirect to `/dashboard`
- **Result**: ✅ Pass

### 3. Admin Dashboard Access
- **Test**: Login as admin and access `/dashboard`
- **Expected**: Display admin dashboard with management features
- **Result**: ✅ Pass

### 4. Student Dashboard Access
- **Test**: Login as student and access `/dashboard`
- **Expected**: Display student dashboard with application features
- **Result**: ✅ Pass

### 5. Invalid Token Handling
- **Test**: Access `/dashboard` with expired/invalid JWT
- **Expected**: Redirect to `/auth/login`
- **Result**: ✅ Pass

---

## Challenges and Solutions

### Challenge 1: Session State Management
**Problem**: Initial confusion about whether to use session-based or token-based authentication  
**Solution**: Adopted JWT token approach with HTTP-only cookies for security and stateless architecture

### Challenge 2: Role-Based Routing Logic
**Problem**: Determining the best approach for routing users to different dashboards  
**Solution**: Implemented simple conditional logic in controller rather than complex security configurations, making it easier to maintain and extend

### Challenge 3: Authentication State Synchronization
**Problem**: Ensuring userEmail attribute is properly set before controller methods execute  
**Solution**: Leveraged Spring's filter chain ordering to ensure JwtAuthenticationFilter runs before controller methods

---

## Code Quality and Best Practices

### Design Patterns Used
1. **Dependency Injection**: AuthService injected via constructor for testability
2. **Single Responsibility**: Controller only handles routing, delegates business logic to service layer
3. **Separation of Concerns**: Authentication logic separated from routing logic

### Code Standards
- ✅ Follows Spring MVC conventions
- ✅ Proper use of annotations (@Controller, @GetMapping)
- ✅ Clear method names and parameter naming
- ✅ Comprehensive null checking and error handling
- ✅ Consistent code formatting and indentation

---

## Performance Considerations

### Database Queries
- Single database query per dashboard access to retrieve user details
- Efficient query using email index (unique constraint on email column)

### Caching Opportunities
- User details could be cached to reduce database queries
- JWT token already contains basic user info, reducing need for database access

---

## Future Enhancements

### 1. Company Role Support
Add routing for COMPANY role users to company-specific dashboard:
```java
if ("COMPANY".equals(user.getRole())) {
    return "dashboard/company-dashboard";
}
```

### 2. User Activity Logging
Track dashboard access in the `login_attempts` or new `user_activity` table for analytics

### 3. Dashboard Personalization
Allow users to customize their dashboard layout and preferences

### 4. Role Hierarchy
Implement role inheritance (e.g., SUPER_ADMIN inherits ADMIN permissions)

---

## Metrics and Statistics

| Metric | Value |
|--------|-------|
| Lines of Code | ~50 lines |
| Methods Implemented | 2 |
| Integration Points | 3 (AuthService, JwtFilter, Templates) |
| Test Scenarios Covered | 5 |
| Security Checks | 4 (auth validation, user existence, null checks, fail-safe redirects) |

---

## Lessons Learned

1. **Security First**: Always implement multiple layers of authentication validation
2. **User Experience**: Seamless redirects improve user experience (auto-redirect authenticated users)
3. **Maintainability**: Simple, clear code is easier to maintain than complex security configurations
4. **Integration**: Understanding the entire authentication flow is crucial for proper implementation
5. **Testing**: Manual testing of all user scenarios revealed edge cases in authentication flow

---

## Collaboration and Teamwork

### Dependencies on Other Team Members
- **Authentication Team**: Relied on JWT token implementation and AuthService
- **Frontend Team**: Coordinated on data requirements for dashboard templates
- **Database Team**: Ensured user entity schema matches controller expectations

### Code Reviews Participated
- Reviewed authentication filter implementation
- Provided feedback on security configuration
- Suggested improvements to error handling in AuthService

---

## Conclusion

The HomeController implementation successfully provides a secure, role-based routing mechanism for the SIMS application. The solution is:
- **Secure**: Multiple layers of authentication validation
- **Scalable**: Easy to add new roles and dashboards
- **Maintainable**: Clear, simple code following Spring MVC best practices
- **User-friendly**: Seamless navigation experience for authenticated users

This component serves as a critical entry point for the application, ensuring users are properly authenticated and routed to the appropriate dashboard based on their role in the system.

---

## References

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [Spring MVC Controller Documentation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- Project README: [`README.md`](../README.md)

---