package com.example.sims.controller;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

import com.example.sims.entity.ApplicationEntity;
import com.example.sims.entity.CompanyEntity;
import com.example.sims.entity.InternshipEntity;
import com.example.sims.entity.UserEntity;
// import com.example.sims.service.AdminService;
import com.example.sims.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final com.example.sims.service.AdminService adminService;
    private final com.example.sims.repo.CompanyRepository companyRepository;

    public AdminController(AuthService authService, com.example.sims.service.AdminService adminService,
            com.example.sims.repo.CompanyRepository companyRepository) {
        this.authService = authService;
        this.adminService = adminService;
        this.companyRepository = companyRepository;
    }

    private UserEntity getAuthenticatedAdmin(HttpServletRequest request) {
        String userEmail = (String) request.getAttribute("userEmail");
        if (userEmail == null) {
            return null;
        }
        UserEntity user = authService.getUserByEmail(userEmail);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return null;
        }
        return user;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null) {
            return "redirect:/auth/login";
        }

        return "dashboard/admin-dashboard";
    }

    @GetMapping("/manage-internships")
    public String manageInternships(HttpServletRequest request, Model model) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null) {
            return "redirect:/auth/login";
        }

        return "admin-template/manage-internships";
    }

    @GetMapping("/manage-internships/fragment")
    public String manageInternshipsFragment(HttpServletRequest request, Model model) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null) {
            return "redirect:/auth/login";
        }
        // optionally add data from service later
        return "admin-template/manage-internships-fragment";
    }

    @GetMapping("/api/active-users")
    public org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String, Object>>> getActiveUsers(
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        java.util.List<UserEntity> users = adminService.getAllUsers();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (UserEntity u : users) {
            if (Boolean.TRUE.equals(u.getActive())) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("email", u.getEmail());
                m.put("role", u.getRole());
                m.put("createdAt", u.getCreatedAt());
                out.add(m);
            }
        }
        return org.springframework.http.ResponseEntity.ok(out);
    }

    @GetMapping("/api/users")
    public org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAllUsersApi(
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        java.util.List<UserEntity> users = adminService.getAllUsers();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (UserEntity u : users) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("role", u.getRole());
            m.put("active", u.getActive());
            m.put("createdAt", u.getCreatedAt());
            out.add(m);
        }
        return org.springframework.http.ResponseEntity.ok(out);
    }

    @GetMapping("/api/internships")
    public org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String, Object>>> getInternshipsApi(
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        java.util.List<com.example.sims.entity.InternshipEntity> list = adminService.getAllInternships();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (com.example.sims.entity.InternshipEntity i : list) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", i.getId());
            m.put("title", i.getTitle());
            m.put("company", i.getCompanyName());
            m.put("status", i.getStatus());
            m.put("closingDate", i.getClosingDate());
            out.add(m);
        }
        return org.springframework.http.ResponseEntity.ok(out);
    }

    @DeleteMapping("/api/internships/{id}")
    public org.springframework.http.ResponseEntity<Void> deleteInternship(@PathVariable Long id,
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        try {
            adminService.deleteInternship(id);
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/api/internships")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> createInternship(
            @RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        try {
            InternshipEntity it = new InternshipEntity();
            it.setTitle((String) body.getOrDefault("title", ""));
            it.setDescription((String) body.getOrDefault("description", ""));
            it.setLocation((String) body.getOrDefault("location", ""));
            Object seatsObj = body.get("seats");
            if (seatsObj != null)
                it.setSeats(((Number) seatsObj).intValue());
            // NOTE: company is not set here; admin UI should set company via companyId when
            // available
            InternshipEntity created = adminService.createInternship(it);
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", created.getId());
            return org.springframework.http.ResponseEntity.ok(m);
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/api/internships/{id}")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> updateInternship(
            @PathVariable Long id, @RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        try {
            java.util.Optional<InternshipEntity> opt = adminService.getAllInternships().stream()
                    .filter(i -> i.getId() != null && i.getId().equals(id)).findFirst();
            if (!opt.isPresent())
                return org.springframework.http.ResponseEntity.status(404).build();
            InternshipEntity it = opt.get();
            if (body.containsKey("title"))
                it.setTitle((String) body.get("title"));
            if (body.containsKey("description"))
                it.setDescription((String) body.get("description"));
            if (body.containsKey("location"))
                it.setLocation((String) body.get("location"));
            if (body.containsKey("seats") && body.get("seats") != null)
                it.setSeats(((Number) body.get("seats")).intValue());
            InternshipEntity updated = adminService.updateInternship(it);
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", updated.getId());
            return org.springframework.http.ResponseEntity.ok(m);
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/api/overview")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> getOverview(
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("totalStudents", adminService.getActiveStudentsCount());
        m.put("totalInternships", adminService.getTotalInternships());
        m.put("totalCompanies", adminService.getTotalCompanies());
        m.put("pendingApplications", adminService.getPendingApplications());
        return org.springframework.http.ResponseEntity.ok(m);
    }

    @GetMapping("/api/companies")
    public org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String, Object>>> getCompaniesApi(
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        java.util.List<CompanyEntity> companies = companyRepository.findAll();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (CompanyEntity c : companies) {
            java.util.Map<String, Object> mm = new java.util.HashMap<>();
            mm.put("id", c.getId());
            mm.put("companyName", c.getCompanyName());
            mm.put("contactEmail", c.getContactEmail());
            mm.put("contactPhone", c.getContactPhone());
            mm.put("address", c.getAddress());
            mm.put("userId", c.getUser() != null ? c.getUser().getId() : null);
            out.add(mm);
        }
        return org.springframework.http.ResponseEntity.ok(out);
    }

    @GetMapping("/api/applications")
    public org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String, Object>>> getApplicationsApi(
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        java.util.List<ApplicationEntity> apps = adminService.getAllApplications();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (ApplicationEntity a : apps) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", a.getId());
            if (a.getStudent() != null) {
                m.put("studentId", a.getStudent().getId());
                m.put("studentName", a.getStudent().getFullName() != null ? a.getStudent().getFullName()
                        : (a.getStudent().getUser() != null ? a.getStudent().getUser().getUsername() : null));
                m.put("studentEmail", a.getStudent().getUser() != null ? a.getStudent().getUser().getEmail() : null);
            }
            if (a.getInternship() != null) {
                m.put("internshipId", a.getInternship().getId());
                m.put("internshipTitle", a.getInternship().getTitle());
                m.put("companyName",
                        a.getInternship().getCompany() != null ? a.getInternship().getCompany().getCompanyName()
                                : null);
            }
            m.put("status", a.getStatus() != null ? a.getStatus().name() : null);
            m.put("appliedAt", a.getAppliedAt());
            out.add(m);
        }
        return org.springframework.http.ResponseEntity.ok(out);
    }

    @DeleteMapping("/api/users/{id}")
    public org.springframework.http.ResponseEntity<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        try {
            adminService.deleteUser(id);
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/api/users/{id}")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> updateUser(@PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return org.springframework.http.ResponseEntity.status(401).build();
        try {
            String username = body.containsKey("username") ? (String) body.get("username") : null;
            String email = body.containsKey("email") ? (String) body.get("email") : null;
            String role = body.containsKey("role") ? (String) body.get("role") : null;
            Boolean active = body.containsKey("active") ? (Boolean) body.get("active") : null;
            UserEntity updated = adminService.updateUser(id, username, email, role, active);
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", updated.getId());
            m.put("username", updated.getUsername());
            m.put("email", updated.getEmail());
            m.put("role", updated.getRole());
            m.put("active", updated.getActive());
            m.put("createdAt", updated.getCreatedAt());
            return org.springframework.http.ResponseEntity.ok(m);
        } catch (IllegalArgumentException iae) {
            return org.springframework.http.ResponseEntity.status(404).build();
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/stream/active-users")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamActiveUsers(
            HttpServletRequest request) {
        UserEntity admin = getAuthenticatedAdmin(request);
        if (admin == null)
            return null;
        return com.example.sims.realtime.ActiveUserNotifier.register();
    }

}