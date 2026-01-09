package com.example.sims.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sims.entity.ApplicationEntity;
import com.example.sims.entity.CompanyEntity;
import com.example.sims.entity.InternshipEntity;
import com.example.sims.repo.ApplicationRepository;
import com.example.sims.repo.CompanyRepository;
import com.example.sims.repo.InternshipRepository;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;

    public CompanyService(CompanyRepository companyRepository,
                          InternshipRepository internshipRepository,
                          ApplicationRepository applicationRepository) {
        this.companyRepository = companyRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
    }

    public Optional<CompanyEntity> getCompanyByUserId(Long userId) {
        return companyRepository.findByUserId(userId);
    }

    public List<InternshipEntity> getCompanyInternships(Long companyId) {
        return internshipRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<ApplicationEntity> getCompanyApplications(Long companyId) {
        return applicationRepository.findByCompanyId(companyId);
    }

    public List<ApplicationEntity> getInternshipApplications(Long internshipId) {
        return applicationRepository.findByInternshipId(internshipId);
    }

    public Long getTotalApplications(Long companyId) {
        return applicationRepository.countByCompanyId(companyId);
    }

    public Long getPendingApplications(Long companyId) {
        return applicationRepository.countPendingByCompanyId(companyId);
    }

    @Transactional
    public InternshipEntity createInternship(InternshipEntity internship) {
        return internshipRepository.save(internship);
    }

    @Transactional
    public InternshipEntity updateInternship(InternshipEntity internship) {
        return internshipRepository.save(internship);
    }

    @Transactional
    public void deleteInternship(Long internshipId) {
        internshipRepository.deleteById(internshipId);
    }

    @Transactional
    public CompanyEntity updateCompanyProfile(CompanyEntity company) {
        return companyRepository.save(company);
    }

    public Optional<InternshipEntity> getInternshipById(Long internshipId) {
        return internshipRepository.findById(internshipId);
    }

    public Optional<ApplicationEntity> getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId);
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
}
