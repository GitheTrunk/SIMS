package com.example.sims.repository;

import com.example.sims.model.InternshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<InternshipApplication, Long> {
}
