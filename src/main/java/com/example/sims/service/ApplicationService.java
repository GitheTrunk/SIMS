package com.example.sims.service;

import com.example.sims.model.*;
import com.example.sims.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final EvaluationRepository evaluationRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
            StudentRepository studentRepository,
            CompanyRepository companyRepository,
            EvaluationRepository evaluationRepository) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public InternshipApplication submitApplication(Long studentId, Long companyId, String positionTitle) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        InternshipApplication app = new InternshipApplication(student, company, positionTitle);
        app.setStatus(ApplicationStatus.SUBMITTED);
        return applicationRepository.save(app);
    }

    @Transactional
    public InternshipApplication supervisorReview(Long applicationId, String comment, boolean accept) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        app.setSupervisorComment(comment);
        if (accept) {
            app.setStatus(ApplicationStatus.REVIEWED_BY_COMPANY);
        } else {
            app.setStatus(ApplicationStatus.REJECTED);
        }
        return applicationRepository.save(app);
    }

    @Transactional
    public InternshipApplication facultyApprove(Long applicationId, boolean approve) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (approve)
            app.setStatus(ApplicationStatus.APPROVED_BY_FACULTY);
        else
            app.setStatus(ApplicationStatus.REJECTED);
        return applicationRepository.save(app);
    }

    @Transactional
    public InternshipApplication adminApprove(Long applicationId, boolean approve) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (approve)
            app.setStatus(ApplicationStatus.APPROVED_BY_ADMIN);
        else
            app.setStatus(ApplicationStatus.REJECTED);
        return applicationRepository.save(app);
    }

    @Transactional
    public Evaluation recordEvaluation(Long applicationId, Integer score, String comments) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        Evaluation ev = new Evaluation(app, score, comments);
        return evaluationRepository.save(ev);
    }

    public Optional<InternshipApplication> findApplication(Long id) {
        return applicationRepository.findById(id);
    }
}
