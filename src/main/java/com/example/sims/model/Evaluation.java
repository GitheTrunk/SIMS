package com.example.sims.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private InternshipApplication application;

    @Column
    private Integer score;

    @Column(length = 2000)
    private String comments;

    @Column(nullable = false)
    private Instant evaluatedAt = Instant.now();

    public Evaluation() {
    }

    public Evaluation(InternshipApplication application, Integer score, String comments) {
        this.application = application;
        this.score = score;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InternshipApplication getApplication() {
        return application;
    }

    public void setApplication(InternshipApplication application) {
        this.application = application;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
