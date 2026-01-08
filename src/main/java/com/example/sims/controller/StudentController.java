
package com.example.sims.controller;

import java.util.Optional;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.sims.entity.StudentProfileEntity;
import com.example.sims.entity.UserEntity;
import com.example.sims.entity.InternshipEntity;
import com.example.sims.entity.ApplicationEntity;
import com.example.sims.service.AuthService;
import com.example.sims.service.StudentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final AuthService authService;

    public StudentController(StudentService studentService, AuthService authService) {
        this.studentService = studentService;
        this.authService = authService;
    }

    private StudentProfileEntity getAuthenticatedStudent(HttpServletRequest request) {
        String userEmail = (String) request.getAttribute("userEmail");
        if (userEmail == null) {
            return null;
        }

        UserEntity user = authService.getUserByEmail(userEmail);
        if (user == null) {
            return null;
        }

        Optional<StudentProfileEntity> studentOpt = studentService.getStudentByUserId(user.getId());
        return studentOpt.orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        // 1. Get Authentication
        String userEmail = (String) request.getAttribute("userEmail");
        if (userEmail == null)
            return "redirect:/auth/login";

        UserEntity user = authService.getUserByEmail(userEmail);
        if (user == null)
            return "redirect:/auth/login";

        // 2. Get Student Profile
        StudentProfileEntity student = getAuthenticatedStudent(request);

        if (student != null) {
            // Fetch student's specific applications
            List<ApplicationEntity> apps = studentService.getStudentApplications(student.getId());
            model.addAttribute("applications", apps);
            model.addAttribute("applicationCount", apps.size());

            // --- START NEW LOGIC FOR AVAILABLE INTERNSHIPS ---
            // Fetch all internships available in the system
            List<InternshipEntity> allInternships = studentService.getAllInternships();

            // Calculate Available: Total system internships - Number student applied for
            int availableCount = Math.max(0, allInternships.size() - apps.size());
            model.addAttribute("availableCount", availableCount);
            // --- END NEW LOGIC ---

            // Count Approved
            long approvedCount = apps.stream()
                    .filter(a -> a.getStatus() == ApplicationEntity.ApplicationStatus.APPROVED)
                    .count();
            model.addAttribute("approvedCount", approvedCount);

            // Count Pending
            long pendingCount = apps.stream()
                    .filter(a -> a.getStatus() == ApplicationEntity.ApplicationStatus.PENDING)
                    .count();
            model.addAttribute("pendingCount", pendingCount);

            // Profile data
            model.addAttribute("student", student);
            model.addAttribute("fullname", student.getFullName());
            model.addAttribute("major", student.getMajor());
            model.addAttribute("year", student.getYear());
            model.addAttribute("studentCode", student.getStudentCode());
            model.addAttribute("email", userEmail);
        }

        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());

        return "dashboard/user-dashboard";
    }

    @GetMapping("/user-profile")
    public String userProfile(HttpServletRequest request, Model model) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }
        String userEmail = (String) request.getAttribute("userEmail");
        model.addAttribute("student", student);
        model.addAttribute("email", userEmail);
        model.addAttribute("fullname", student.getFullName());
        model.addAttribute("major", student.getMajor());
        model.addAttribute("year", student.getYear());
        model.addAttribute("studentCode", student.getStudentCode());
        model.addAttribute("cvFile", student.getCvFile());
        return "user-template/user-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfile(HttpServletRequest request,
            @RequestParam String fullname,
            @RequestParam String major,
            @RequestParam Integer year,
            RedirectAttributes redirectAttributes) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }

        student.setFullName(fullname);
        student.setMajor(major);
        student.setYear(year);
        studentService.updateStudentProfile(student);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/student/user-profile";
    }

    @GetMapping("/browse-internship")
    public String browseInternship(HttpServletRequest request, Model model) {
        // Add authenticated student info (if available)
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student != null) {
            model.addAttribute("student", student);
        }

        // Load internships for students to browse
        model.addAttribute("internships", studentService.getAllInternships());
        return "user-template/browse-internship";
    }

    @GetMapping("/user-application")
    public String userApplication(HttpServletRequest request, Model model) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }

        // Fetch all applications for this student
        model.addAttribute("student", student);
        model.addAttribute("applications", studentService.getStudentApplications(student.getId()));
        return "user-template/user-application";
    }

    @GetMapping("/user-application/pending")
    public String viewPendingApplications(HttpServletRequest request, Model model) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null)
            return "redirect:/auth/login";

        List<ApplicationEntity> allApps = studentService.getStudentApplications(student.getId());

        // Filter logic
        List<ApplicationEntity> pendingApps = allApps.stream()
                .filter(a -> a.getStatus() == ApplicationEntity.ApplicationStatus.PENDING)
                .toList();

        model.addAttribute("applications", pendingApps);
        model.addAttribute("student", student);

        // Make sure this matches your filename exactly
        return "user-template/pending-application";
    }

    @GetMapping("/user-application/approved")
    public String viewApprovedApplications(HttpServletRequest request, Model model) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null)
            return "redirect:/auth/login";

        List<ApplicationEntity> allApps = studentService.getStudentApplications(student.getId());

        // Filter for APPROVED status only
        List<ApplicationEntity> approvedApps = allApps.stream()
                .filter(a -> a.getStatus() == ApplicationEntity.ApplicationStatus.APPROVED)
                .toList();

        model.addAttribute("applications", approvedApps);
        model.addAttribute("student", student);
        return "user-template/approved-application";
    }

    @GetMapping("/apply/{internshipId}")
    public String applyForm(@PathVariable Long internshipId, HttpServletRequest request, Model model) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }

        Optional<InternshipEntity> internshipOpt = studentService.getInternshipById(internshipId);
        if (!internshipOpt.isPresent()) {
            return "redirect:/student/browse-internship";
        }

        InternshipEntity internship = internshipOpt.get();

        // Check if student has already applied
        Optional<ApplicationEntity> existingApp = studentService.getStudentApplicationForInternship(student.getId(),
                internshipId);
        if (existingApp.isPresent()) {
            return "redirect:/student/browse-internship";
        }

        model.addAttribute("student", student);
        model.addAttribute("internship", internship);
        return "user-template/apply-internship";
    }

    @PostMapping("/apply/{internshipId}")
    public String submitApplication(@PathVariable Long internshipId,
            HttpServletRequest request,
            @RequestParam String coverLetter,
            RedirectAttributes redirectAttributes) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }

        Optional<InternshipEntity> internshipOpt = studentService.getInternshipById(internshipId);
        if (!internshipOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Internship not found");
            return "redirect:/student/browse-internship";
        }

        InternshipEntity internship = internshipOpt.get();

        // Check if student has already applied
        Optional<ApplicationEntity> existingApp = studentService.getStudentApplicationForInternship(student.getId(),
                internshipId);
        if (existingApp.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "You have already applied for this internship");
            return "redirect:/student/browse-internship";
        }

        try {
            ApplicationEntity application = new ApplicationEntity(student, internship);
            application.setCoverLetter(coverLetter);
            studentService.createApplication(application);

            redirectAttributes.addFlashAttribute("success", "Application submitted successfully!");
            return "redirect:/student/user-application";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit application: " + e.getMessage());
            return "redirect:/student/browse-internship";
        }
    }

    @GetMapping("/upload-cv")
    public String uploadCvPage(HttpServletRequest request, Model model) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("student", student);
        model.addAttribute("cvFile", student.getCvFile());
        return "user-template/upload-cv";
    }

    @PostMapping("/upload-cv")
    public String uploadCv(HttpServletRequest request,
            @RequestParam("cvFile") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/student/user-profile";
        }

        try {
            // Delete old CV if exists
            if (student.getCvFile() != null && !student.getCvFile().isEmpty()) {
                try {
                    studentService.deleteCvFile(student.getCvFile());
                } catch (Exception e) {
                    System.err.println("Warning: Failed to delete old CV: " + e.getMessage());
                }
            }

            // Upload new CV
            String fileName = studentService.uploadCvFile(file, student.getId());
            student.setCvFile(fileName);
            studentService.updateStudentProfile(student);
            redirectAttributes.addFlashAttribute("success", "CV uploaded successfully!");
            return "redirect:/student/user-profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload CV: " + e.getMessage());
            return "redirect:/student/user-profile";
        }
    }

    @PostMapping("/delete-cv")
    public String deleteCv(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null) {
            return "redirect:/auth/login";
        }

        if (student.getCvFile() == null || student.getCvFile().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No CV file to delete");
            return "redirect:/student/user-profile";
        }

        try {
            studentService.deleteCvFile(student.getCvFile());
            student.setCvFile(null);
            studentService.updateStudentProfile(student);
            redirectAttributes.addFlashAttribute("success", "CV deleted successfully!");
            return "redirect:/student/user-profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete CV: " + e.getMessage());
            return "redirect:/student/user-profile";
        }
    }

    @GetMapping("/view-cv")
    public ResponseEntity<byte[]> viewCv(HttpServletRequest request) {
        StudentProfileEntity student = getAuthenticatedStudent(request);
        if (student == null || student.getCvFile() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get("uploads/cv/" + student.getCvFile());
            File file = filePath.toFile();

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            String fileName = student.getCvFile();

            // Determine content type based on file extension
            MediaType contentType = MediaType.APPLICATION_PDF;
            if (fileName.endsWith(".doc")) {
                contentType = MediaType.valueOf("application/msword");
            } else if (fileName.endsWith(".docx")) {
                contentType = MediaType
                        .valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(contentType)
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}