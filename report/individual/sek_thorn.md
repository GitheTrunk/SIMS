# Individual Report: SEK Thorn

---

## ✅ Executive Summary

**Implemented** stateless authentication for SIMS using Spring Security and JWT; **documented** authentication and authorization flows with PlantUML. The system enforces role-based access (ROLE_ADMIN, ROLE_USER) and issues/validates JWTs in a custom security filter.

---

## 🎯 Objective

- Capture authentication and authorization architecture with PlantUML diagrams.
- Implement secure, stateless login using JWT.
- Configure `SecurityConfig` to protect endpoints and enforce roles.

---

## 🖼️ PlantUML Artifacts

- Key files:
  - `plant_uml/usecase-sims.puml` — actor and use-case view.
  - `plant_uml/sequence-apply_user.puml` & `plant_uml/sequence_apply_admin.puml` — login, token issuance, and protected actions.
  - `plant_uml/db_sims.puml` — users, roles, applications ER view.

> Purpose: Visualize login → JWT issuance → resource access and role-based branching for clear design review and tests.

---

## 🔧 Implementation — Spring Security & `SecurityConfig`

**Design decisions:**

- Stateless sessions (JWT): `SessionCreationPolicy.STATELESS`.
- Passwords hashed with `BCryptPasswordEncoder`.
- `UserDetailsService` fetches users and roles from DB.
- Custom `JwtFilter` validates tokens and sets the security context.

**Example configuration:**

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
            .antMatchers("/auth/**", "/public/**").permitAll()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        .and()
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
}
```

**Important beans:** `PasswordEncoder`, `AuthenticationManager`, `UserDetailsService`, `JwtFilter`, `JwtUtil`.

---

## 🔐 JWT Design & Flow

- **Claims:** `sub` (username), `iat`, `exp`, plus custom `roles` and `userId`.
- **Signature:** HS256 (HMAC) in dev; recommend RS256 (RSA) with key rotation in production.
- **Lifecycle:**
  1. User authenticates at `/auth/login` → server authenticates credentials.
  2. Server issues JWT (short-lived access token ± refresh token).
  3. Client sends `Authorization: Bearer <token>` for protected endpoints.
  4. `JwtFilter` extracts token, validates signature/expiry, and populates `SecurityContext`.

**Utility methods (concept):**

```java
String generateToken(UserDetails user);
boolean validateToken(String token, UserDetails user);
String extractUsername(String token);
List<String> extractRoles(String token);
```

---

## 🧪 Testing & Validation

- Manual Postman tests: login → receive token → access endpoints with `Authorization` header.
- Unit tests: token generation/validation, `JwtFilter` behavior.
- Integration tests: ensure protected endpoints allow/deny correctly based on roles.

---

## ⚠️ Challenges & Mitigations

- **Revocation:** Stateless tokens cannot be easily revoked — mitigate with short-lived tokens + refresh tokens and server-side revoke list.
- **Secret management:** Secret moved to environment variable; recommend using Vault/KMS for production.
- **CORS/CSRF:** CSRF disabled for stateless API; configure CORS for front-end origin.

---

## ✅ Recommendations & Next Steps

1. Switch to RS256 with key rotation and public-key distribution.
2. Implement refresh tokens with revocation mechanisms.
3. Add end-to-end tests for expired/invalid tokens and role mismatches.
4. Update PlantUML diagrams to include refresh/revocation flows and publish them in project docs.

---

## 🧾 Conclusion

The SIMS application now has a documented, testable, and secure stateless authentication layer using Spring Security and JWT. With recommended hardening (asymmetric signing, refresh tokens, secret management), it will be production-ready.

---

**Next actions:**

- Reply with the items you want next: add code snippets/tests here, embed PlantUML diagrams, or commit & push the file to Git.
