CREATE TABLE placements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    coordinator_id BIGINT NOT NULL,
    confirmed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_placement_student
        FOREIGN KEY (student_id)
        REFERENCES users(id),

    CONSTRAINT fk_placement_internship
        FOREIGN KEY (internship_id)
        REFERENCES internships(id),

    CONSTRAINT fk_placement_coordinator
        FOREIGN KEY (coordinator_id)
        REFERENCES users(id)
);