package com.example.sims.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sims.dto.AuthResponse;
import com.example.sims.entity.UserEntity;
import com.example.sims.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model,
            HttpServletResponse response) {
        try {
            String token = authService.login(email, password);

            // Set JWT token in cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(86400);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String username,
            @RequestParam String password, @RequestParam(required = false) String role, Model model) {
        try {
            authService.register(email, username, password, role != null ? role : "USER");
            model.addAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/auth/login";
    }

    // REST API endpoints for API-based authentication
    @RestController
    @RequestMapping("/api/auth")
    public class AuthApiController {

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> apiLogin(@RequestParam String email,
                @RequestParam String password) {
            try {
                String token = authService.login(email, password);
                UserEntity user = authService.getUserByEmail(email);
                return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
            } catch (Exception e) {
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, null, null, e.getMessage()));
            }
        }

        @PostMapping("/register")
        public ResponseEntity<AuthResponse> apiRegister(@RequestParam String email,
                @RequestParam String username, @RequestParam String password,
                @RequestParam(required = false) String role) {
            try {
                UserEntity user = authService.register(email, username, password,
                        role != null ? role : "USER");
                String token = authService.login(email, password);
                return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
            } catch (Exception e) {
                return ResponseEntity.status(400)
                        .body(new AuthResponse(null, null, null, e.getMessage()));
            }
        }
    }
}