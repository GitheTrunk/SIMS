package com.example.sims.controller;

import com.example.sims.model.Role;
import com.example.sims.model.User;
import com.example.sims.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignupController {

    private final UserRepository userRepository;

    public SignupController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String role,
            Model model) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Email and password are required");
            return "signup";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "An account with that email already exists");
            return "signup";
        }

        Role r = Role.STUDENT;
        if (role != null) {
            try {
                r = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        User u = new User(email, password, r);
        userRepository.save(u);
        return "redirect:/login";
    }
}
