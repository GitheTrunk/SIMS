package com.example.sims.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sims.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    
}
