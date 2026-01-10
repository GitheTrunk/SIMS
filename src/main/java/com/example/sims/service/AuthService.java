package com.example.sims.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sims.entity.CompanyEntity;
import com.example.sims.entity.StudentProfileEntity;
import com.example.sims.entity.UserEntity;
import com.example.sims.repo.CompanyRepository;
import com.example.sims.repo.StudentProfileRepository;
import com.example.sims.repo.UserRepository;
import com.example.sims.security.JwtTokenProvider;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, StudentProfileRepository studentProfileRepository,
            CompanyRepository companyRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserEntity register(String email, String username, String password, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        UserEntity user = new UserEntity(email, username, passwordEncoder.encode(password),
                role != null ? role : "USER");
        user = userRepository.save(user);

        // Create profile based on role
        if ("USER".equals(role) || "STUDENT".equals(role)) {
            // Create student profile
            String studentCode = "STU-" + String.format("%05d", user.getId());
            StudentProfileEntity profile = new StudentProfileEntity(user, studentCode, username);
            studentProfileRepository.save(profile);
        } else if ("COMPANY".equals(role)) {
            // Create company profile
            CompanyEntity company = new CompanyEntity(user, username);
            companyRepository.save(company);
        }

        return user;
    }

    public String login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return jwtTokenProvider.generateToken(email, user.getRole());
    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void markUserActiveByEmail(String email, boolean active) {
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setActive(active);
            userRepository.save(u);
            try {
                com.example.sims.realtime.ActiveUserNotifier.notifyChange();
            } catch (Exception ex) {
                /* ignore notifier failures */ }
        });
    }

    public UserEntity updateUser(Long id, String username, String email) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
                "User not found"));
        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        return userRepository.save(user);
    }
}
