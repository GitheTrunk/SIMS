package com.example.sims.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.sims.entity.ApplicationEntity;
import com.example.sims.entity.InternshipEntity;
import com.example.sims.entity.UserEntity;
import com.example.sims.repo.ApplicationRepository;
import com.example.sims.repo.InternshipRepository;
import com.example.sims.repo.UserRepository;
import com.example.sims.repo.CompanyRepository;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;
    private final com.example.sims.repo.CompanyRepository companyRepository;

    public AdminService(UserRepository userRepository,
            InternshipRepository internshipRepository,
            ApplicationRepository applicationRepository,
            CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity setUserActive(Long id, boolean active) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(active);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserEntity updateUser(Long id, String username, String email, String role, Boolean active) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (username != null)
            user.setUsername(username);
        if (email != null)
            user.setEmail(email);
        if (role != null)
            user.setRole(role);
        if (active != null)
            user.setActive(active);
        return userRepository.save(user);
    }

    public long getTotalStudents() {
        return userRepository.findAll().stream().filter(u -> "STUDENT".equals(u.getRole())).count();
    }

    public long getActiveStudentsCount() {
        return userRepository.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole()) && Boolean.TRUE.equals(u.getActive()))
                .count();
    }

    public long getTotalInternships() {
        return internshipRepository.count();
    }

    public long getTotalCompanies() {
        try {
            return companyRepository != null ? companyRepository.count() : 0L;
        } catch (Exception ex) {
            return 0L;
        }
    }

    public long getTotalApplications() {
        return applicationRepository.count();
    }

    public long getPendingApplications() {
        return applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == ApplicationEntity.ApplicationStatus.PENDING)
                .count();
    }

    public List<InternshipEntity> getAllInternships() {
        return internshipRepository.findAll();
    }

    public InternshipEntity createInternship(InternshipEntity internship) {
        return internshipRepository.save(internship);
    }

    public InternshipEntity updateInternship(InternshipEntity internship) {
        return internshipRepository.save(internship);
    }

    public void deleteInternship(Long id) {
        internshipRepository.deleteById(id);
    }

    public List<ApplicationEntity> getAllApplications() {
        return applicationRepository.findAll();
    }
}
