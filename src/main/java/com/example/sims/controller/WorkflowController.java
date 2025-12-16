package com.example.sims.controller;

import com.example.sims.model.Evaluation;
import com.example.sims.model.InternshipApplication;
import com.example.sims.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class WorkflowController {

    private final ApplicationService applicationService;

    public WorkflowController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<InternshipApplication> submit(@RequestParam Long studentId,
            @RequestParam Long companyId,
            @RequestParam String positionTitle) {
        InternshipApplication app = applicationService.submitApplication(studentId, companyId, positionTitle);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<InternshipApplication> review(@PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestParam(defaultValue = "true") boolean accept) {
        InternshipApplication app = applicationService.supervisorReview(id, comment, accept);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{id}/faculty-approve")
    public ResponseEntity<InternshipApplication> facultyApprove(@PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean approve) {
        InternshipApplication app = applicationService.facultyApprove(id, approve);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{id}/admin-approve")
    public ResponseEntity<InternshipApplication> adminApprove(@PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean approve) {
        InternshipApplication app = applicationService.adminApprove(id, approve);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{id}/evaluation")
    public ResponseEntity<Evaluation> evaluate(@PathVariable Long id,
            @RequestParam Integer score,
            @RequestParam(required = false) String comments) {
        Evaluation ev = applicationService.recordEvaluation(id, score, comments);
        return ResponseEntity.ok(ev);
    }
}
