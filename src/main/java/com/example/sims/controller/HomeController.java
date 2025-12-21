package com.example.sims.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/user-dashboard")
    public String dashboard() {
        return "user-dashboard";
    }

    @GetMapping("/user-profile")
    public String profile() {
        return "user-profile";
    }

    @GetMapping("/user-application")
    public String applications() {
        return "user-application";
    }

    @GetMapping("/user-internship")
    public String internships() {
        return "user-internship";
    }
}