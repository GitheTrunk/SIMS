package com.example.sims.repository;

import com.example.sims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom query method to find user by email
    Optional<User> findByEmail(String email);
    
    // Custom query method to find users by role
    java.util.List<User> findByRole(String role);
    
}
