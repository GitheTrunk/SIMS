package com.example.sims.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sims.entity.ApplicationEntity;
import com.example.sims.entity.CompanyEntity;
import com.example.sims.entity.InternshipEntity;
import com.example.sims.entity.UserEntity;
import com.example.sims.service.AuthService;
import com.example.sims.service.CompanyService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/company")
public class CompanyController {
    private final CompanyService companyService;
    private final AuthService authService;

    public CompanyController(CompanyService companyService, AuthService authService) {
        this.companyService = companyService;
        this.authService = authService;
    }

    private CompanyEntity getAuthenticatedCompany(HttpServletRequest request) {
        String userEmail = (String) request.getAttribute("userEmail");
        if (userEmail == null) {
            return null;
        }

        UserEntity user = authService.getUserByEmail(userEmail);
        if (user == null || !"COMPANY".equals(user.getRole())) {
            return null;
        }

        Optional<CompanyEntity> companyOpt = companyService.getCompanyByUserId(user.getId());
        return companyOpt.orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        List<InternshipEntity> internships = companyService.getCompanyInternships(company.getId());
        Long totalApplications = companyService.getTotalApplications(company.getId());
        Long pendingApplications = companyService.getPendingApplications(company.getId());

        model.addAttribute("company", company);
        model.addAttribute("username", company.getCompanyName());
        model.addAttribute("totalInternships", internships.size());
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("recentInternships", internships.stream().limit(5).toList());

        return "company-template/company-dashboard";
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("company", company);
        model.addAttribute("username", company.getCompanyName());
        return "company-template/company-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpServletRequest request,
                                @RequestParam String companyName,
                                @RequestParam String address,
                                @RequestParam String contactEmail,
                                @RequestParam String contactPhone,
                                RedirectAttributes redirectAttributes) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        company.setCompanyName(companyName);
        company.setAddress(address);
        company.setContactEmail(contactEmail);
        company.setContactPhone(contactPhone);
        companyService.updateCompanyProfile(company);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/company/profile";
    }

    @GetMapping("/internships")
    public String internships(HttpServletRequest request, Model model) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        List<InternshipEntity> internships = companyService.getCompanyInternships(company.getId());
        model.addAttribute("company", company);
        model.addAttribute("username", company.getCompanyName());
        model.addAttribute("internships", internships);

        return "company-template/manage-internships";
    }

    @GetMapping("/internships/create")
    public String createInternshipForm(HttpServletRequest request, Model model) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("company", company);
        model.addAttribute("username", company.getCompanyName());
        return "company-template/create-internship";
    }

    @PostMapping("/internships/create")
    public String createInternship(HttpServletRequest request,
                                   @RequestParam String title,
                                   @RequestParam String description,
                                   @RequestParam String location,
                                   @RequestParam Integer seats,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   RedirectAttributes redirectAttributes) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        InternshipEntity internship = new InternshipEntity();
        internship.setCompany(company);
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setLocation(location);
        internship.setSeats(seats);
        internship.setStartDate(startDate);
        internship.setEndDate(endDate);

        companyService.createInternship(internship);
        redirectAttributes.addFlashAttribute("success", "Internship created successfully!");
        return "redirect:/company/internships";
    }

    @GetMapping("/internships/{id}/edit")
    public String editInternshipForm(HttpServletRequest request, @PathVariable Long id, Model model) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        Optional<InternshipEntity> internshipOpt = companyService.getInternshipById(id);
        if (internshipOpt.isEmpty() || !internshipOpt.get().getCompany().getId().equals(company.getId())) {
            return "redirect:/company/internships";
        }

        model.addAttribute("company", company);
        model.addAttribute("username", company.getCompanyName());
        model.addAttribute("internship", internshipOpt.get());
        return "company-template/edit-internship";
    }

    @PostMapping("/internships/{id}/update")
    public String updateInternship(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestParam String title,
                                   @RequestParam String description,
                                   @RequestParam String location,
                                   @RequestParam Integer seats,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   RedirectAttributes redirectAttributes) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        Optional<InternshipEntity> internshipOpt = companyService.getInternshipById(id);
        if (internshipOpt.isEmpty() || !internshipOpt.get().getCompany().getId().equals(company.getId())) {
            return "redirect:/company/internships";
        }

        InternshipEntity internship = internshipOpt.get();
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setLocation(location);
        internship.setSeats(seats);
        internship.setStartDate(startDate);
        internship.setEndDate(endDate);

        companyService.updateInternship(internship);
        redirectAttributes.addFlashAttribute("success", "Internship updated successfully!");
        return "redirect:/company/internships";
    }

    @PostMapping("/internships/{id}/delete")
    public String deleteInternship(HttpServletRequest request, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        Optional<InternshipEntity> internshipOpt = companyService.getInternshipById(id);
        if (internshipOpt.isEmpty() || !internshipOpt.get().getCompany().getId().equals(company.getId())) {
            return "redirect:/company/internships";
        }

        companyService.deleteInternship(id);
        redirectAttributes.addFlashAttribute("success", "Internship deleted successfully!");
        return "redirect:/company/internships";
    }

    @GetMapping("/applications")
    public String applications(HttpServletRequest request, Model model) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        List<ApplicationEntity> applications = companyService.getCompanyApplications(company.getId());
        model.addAttribute("company", company);
        model.addAttribute("username", company.getCompanyName());
        model.addAttribute("applications", applications);

        return "company-template/view-applications";
    }

    @GetMapping("/applications/{id}")
    public String viewApplication(HttpServletRequest request, @PathVariable Long id, Model model) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        Optional<ApplicationEntity> applicationOpt = companyService.getApplicationById(id);
        if (applicationOpt.isEmpty()) {
            return "redirect:/company/applications";
        }

        ApplicationEntity application = applicationOpt.get();
        // Verify the application belongs to this company's internship
        if (!application.getInternship().getCompany().getId().equals(company.getId())) {
            return "redirect:/company/applications";
        }

        model.addAttribute("company", company);
        model.addAttribute("username", company.getCompanyName());
        model.addAttribute("application", application);

        return "company-template/application-detail";
    }

    @PostMapping("/applications/{id}/approve")
    public String approveApplication(HttpServletRequest request, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        Optional<ApplicationEntity> applicationOpt = companyService.getApplicationById(id);
        if (applicationOpt.isEmpty() || 
            !applicationOpt.get().getInternship().getCompany().getId().equals(company.getId())) {
            return "redirect:/company/applications";
        }

        companyService.updateApplicationStatus(id, ApplicationEntity.ApplicationStatus.APPROVED);
        redirectAttributes.addFlashAttribute("success", "Application approved!");
        return "redirect:/company/applications";
    }

    @PostMapping("/applications/{id}/reject")
    public String rejectApplication(HttpServletRequest request, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        CompanyEntity company = getAuthenticatedCompany(request);
        if (company == null) {
            return "redirect:/auth/login";
        }

        Optional<ApplicationEntity> applicationOpt = companyService.getApplicationById(id);
        if (applicationOpt.isEmpty() || 
            !applicationOpt.get().getInternship().getCompany().getId().equals(company.getId())) {
            return "redirect:/company/applications";
        }

        companyService.updateApplicationStatus(id, ApplicationEntity.ApplicationStatus.REJECTED);
        redirectAttributes.addFlashAttribute("success", "Application rejected!");
        return "redirect:/company/applications";
    }
}
