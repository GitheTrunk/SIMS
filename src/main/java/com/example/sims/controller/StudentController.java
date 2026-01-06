package com.example.sims.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.sims.entity.UserEntity;
import com.example.sims.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

@RequestMapping("/student")
public class StudentController {
    private final AuthService authService;

    public StudentController(AuthService authService){
        this.authService = authService;
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

        // Default to user (student) dashboard
        return "dashboard/user-dashboard";
    }

    @GetMapping("/browse-internship")
    public String browseInternship() {
        return "user-template/browse-internship";
    }

    @GetMapping("/user-application")
    public String userApplication() {
        return "user-template/user-application";
    }

    @GetMapping("/user-profile")
    public String userProfile() {
        return "user-template/user-profile";
    }

}