CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT',, 'ADMIN') NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    major VARCHAR(100),
    year INT,
    cv_file VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE internships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(100),
    seats INT NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, internship_id),
    FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (internship_id) REFERENCES internships(id) ON DELETE CASCADE
);

CREATE TABLE placements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    application_id BIGINT UNIQUE NOT NULL,
    admin_id BIGINT NOT NULL,

    placement_date DATE DEFAULT CURRENT_DATE,
    status ENUM('PLACED', 'CANCELLED') DEFAULT 'PLACED',

    FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE,

    FOREIGN KEY (admin_id)
        REFERENCES users(id)
);

CREATE TABLE evaluations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    placement_id BIGINT UNIQUE NOT NULL,
    evaluator_id BIGINT NOT NULL,

    score INT CHECK (score BETWEEN 1 AND 100),
    remarks TEXT,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (placement_id)
        REFERENCES placements(id)
        ON DELETE CASCADE,

    FOREIGN KEY (evaluator_id)
        REFERENCES users(id)
);

CREATE TABLE login_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);