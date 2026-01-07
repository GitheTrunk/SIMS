package com.example.sims.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sims.entity.ApplicationEntity;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
    List<ApplicationEntity> findByStudentId(Long studentId);

    List<ApplicationEntity> findByInternshipId(Long internshipId);

    @Query("SELECT a FROM ApplicationEntity a WHERE a.internship.company.id = :companyId ORDER BY a.appliedAt DESC")
    List<ApplicationEntity> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.internship.company.id = :companyId")
    Long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.internship.company.id = :companyId AND a.status = 'PENDING'")
    Long countPendingByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.student.id = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.student.id = :studentId AND a.status = 'PENDING'")
    Long countPendingByStudentId(@Param("studentId") Long studentId);
}
