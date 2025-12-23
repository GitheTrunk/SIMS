package com.example.sims.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.sims.entity.UserEntity;
import com.example.sims.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {
    private final AuthService authService;

    public HomeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        String userEmail = (String) request.getAttribute("userEmail");

        if (userEmail != null) {
            return "redirect:/dashboard";
        }

        return "index";
    }

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
}
