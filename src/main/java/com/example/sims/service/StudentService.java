
package com.example.sims.service;

import java.util.List;
import java.util.Optional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.sims.entity.ApplicationEntity;
import com.example.sims.entity.StudentProfileEntity;
import com.example.sims.entity.InternshipEntity;
import com.example.sims.repo.ApplicationRepository;
import com.example.sims.repo.StudentProfileRepository;
import com.example.sims.repo.InternshipRepository;

@Service
public class StudentService {
    private final StudentProfileRepository studentProfileRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;

    public StudentService(StudentProfileRepository studentProfileRepository,
                          InternshipRepository internshipRepository,
                          ApplicationRepository applicationRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
    }

    public Optional<StudentProfileEntity> getStudentByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    public List<InternshipEntity> getAllInternships() {
        return internshipRepository.findAll();
    }

    public List<ApplicationEntity> getStudentApplications(Long studentId) {
        return applicationRepository.findByStudentId(studentId);
    }

    public Optional<ApplicationEntity> getStudentApplicationForInternship(Long studentId, Long internshipId) {
        return applicationRepository.findByStudentIdAndInternshipId(studentId, internshipId);
    }

    public Long getTotalApplications(Long studentId) {
        return applicationRepository.countByStudentId(studentId);
    }

    public Long getPendingApplications(Long studentId) {
        return applicationRepository.countPendingByStudentId(studentId);
    }

    @Transactional
    public StudentProfileEntity updateStudentProfile(StudentProfileEntity studentProfile) {
        return studentProfileRepository.save(studentProfile);
    }

    public Optional<InternshipEntity> getInternshipById(Long internshipId) {
        return internshipRepository.findById(internshipId);
    }

    public Optional<ApplicationEntity> getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId);
    }

    @Transactional
    public ApplicationEntity createApplication(ApplicationEntity application) {
        return applicationRepository.save(application);
    }

    @Transactional
    public ApplicationEntity updateApplicationStatus(Long applicationId, ApplicationEntity.ApplicationStatus status) {
        Optional<ApplicationEntity> appOpt = applicationRepository.findById(applicationId);
        if (appOpt.isPresent()) {
            ApplicationEntity application = appOpt.get();
            application.setStatus(status);
            return applicationRepository.save(application);
        }
        return null;
    }

    @Transactional
    public void deleteApplication(Long applicationId) {
        applicationRepository.deleteById(applicationId);
    }

    public String uploadCvFile(MultipartFile file, Long studentId) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("File is empty");
        }

        // Validate file type (PDF, DOC, DOCX only)
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".pdf") && !fileName.endsWith(".doc")
                && !fileName.endsWith(".docx"))) {
            throw new Exception("Only PDF, DOC, and DOCX files are allowed");
        }

        // Create uploads directory if it doesn't exist
        String uploadDir = "uploads/cv/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Delete old CV file if exists
        try {
            String oldCvPattern = "student_" + studentId + "_";
            File[] files = uploadPath.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(oldCvPattern)) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            // Log but continue with upload
            System.err.println("Warning: Failed to delete old CV file: " + e.getMessage());
        }

        // Generate unique filename with timestamp
        String uniqueFileName = "student_" + studentId + "_" + System.currentTimeMillis()
                + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Save file
        Files.write(filePath, file.getBytes());

        return uniqueFileName;
    }

    public void deleteCvFile(String cvFileName) throws Exception {
        Path filePath = Paths.get("uploads/cv/" + cvFileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}
