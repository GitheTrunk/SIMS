CREATE TABLE applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    internship_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    resume_path VARCHAR(255) NOT NULL,
    cover_letter TEXT,
    status ENUM('APPLIED', 'SHORTLISTED', 'REJECTED', 'APPROVED') DEFAULT 'APPLIED',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_application_internship
        FOREIGN KEY (internship_id)
        REFERENCES internships(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_application_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
