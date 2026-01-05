package com.example.sims.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sims.entity.InternshipEntity;

@Repository
public interface InternshipRepository extends JpaRepository<InternshipEntity, Long> {
    List<InternshipEntity> findByCompanyId(Long companyId);
    List<InternshipEntity> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
}
