-- V1__init_schema.sql - Consolidated Baseline Schema
-- Target Dialect: MySQL 8.0+

-- ============================================================================
-- 1. CORE & AUTHENTICATION
-- ============================================================================

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    email VARCHAR(40) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(15) NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_mobile_number (mobile_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE otp_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile_number VARCHAR(15) NOT NULL,
    otp_code VARCHAR(255) NOT NULL,
    purpose VARCHAR(20) NOT NULL DEFAULT 'LOGIN',
    attempt_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_mobile_number (mobile_number),
    INDEX idx_otp_expiry (expires_at),
    INDEX idx_otp_lookup (mobile_number, is_used, expires_at)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(100) NULL,
    device_type VARCHAR(20) NULL,
    token_family_id VARCHAR(100) NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expiry (expires_at)
) ENGINE=InnoDB;

-- ============================================================================
-- 2. ACADEMIC STRUCTURE
-- ============================================================================

CREATE TABLE schools (
    id BINARY(16) PRIMARY KEY,
    school_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    registration_number VARCHAR(50) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    timezone VARCHAR(50),
    board VARCHAR(50),
    contact_person VARCHAR(100),
    contact_number VARCHAR(15),
    contact_email VARCHAR(100),
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE school_users (
    id BINARY(16) PRIMARY KEY,
    school_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_school_users_school_id FOREIGN KEY (school_id) REFERENCES schools (id),
    CONSTRAINT fk_school_users_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_school_users_school_user UNIQUE (school_id, user_id)
) ENGINE=InnoDB;

CREATE TABLE classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB;

CREATE TABLE teachers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(50) NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    qualification VARCHAR(100) NULL,
    profile_image VARCHAR(500) NULL,
    teacher_id VARCHAR(50) NULL UNIQUE,
    joining_date VARCHAR(50) NULL,
    user_id BINARY(16),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_teachers_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_teachers_deleted (deleted)
) ENGINE=InnoDB;

CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    class_id BIGINT,
    user_id BINARY(16) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_students_class_id FOREIGN KEY (class_id) REFERENCES classes (id),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE INDEX idx_student_user_id (user_id)
) ENGINE=InnoDB;

CREATE TABLE parents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NULL,
    mobile_number VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) NULL,
    address TEXT NULL,
    occupation VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE parent_student_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    relationship_type VARCHAR(20) NOT NULL DEFAULT 'GUARDIAN',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_date DATE NULL,
    end_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent_link_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_link_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT uk_parent_student UNIQUE (parent_id, student_id)
) ENGINE=InnoDB;

-- ============================================================================
-- 3. ACADEMIC OPERATIONS
-- ============================================================================

CREATE TABLE subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB;

CREATE TABLE class_subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    teacher_id BIGINT REFERENCES teachers(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(class_id, subject_id)
) ENGINE=InnoDB;

CREATE TABLE teacher_specializations (
    teacher_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (teacher_id, subject_id),
    CONSTRAINT fk_teacher_spec_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_spec_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE exam_terms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB;

CREATE TABLE exam_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_term_id BIGINT NOT NULL REFERENCES exam_terms(id),
    class_id BIGINT NOT NULL REFERENCES classes(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    max_marks INTEGER NOT NULL,
    exam_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(exam_term_id, class_id, subject_id)
) ENGINE=InnoDB;

CREATE TABLE student_marks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    exam_schedule_id BIGINT NOT NULL REFERENCES exam_schedules(id),
    marks_obtained DOUBLE PRECISION NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(student_id, exam_schedule_id)
) ENGINE=InnoDB;

CREATE TABLE homework (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATE NOT NULL,
    attachment_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB;

CREATE TABLE homework_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    homework_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submission_file_urls JSON NULL,
    remarks TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    grade VARCHAR(10) NULL,
    teacher_remarks TEXT NULL,
    graded_at TIMESTAMP NULL,
    graded_by_user_id BINARY(16) NULL,
    is_late BOOLEAN NOT NULL DEFAULT FALSE,
    resubmission_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_homework_submission_homework FOREIGN KEY (homework_id) REFERENCES homework(id) ON DELETE CASCADE,
    CONSTRAINT fk_homework_submission_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_homework_submission_graded_by FOREIGN KEY (graded_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uk_homework_student UNIQUE (homework_id, student_id)
) ENGINE=InnoDB;

CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks VARCHAR(255),
    marked_by VARCHAR(100),
    modified_by VARCHAR(100),
    is_modified BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attendance_student_id FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_attendance_class_id FOREIGN KEY (class_id) REFERENCES classes (id),
    CONSTRAINT uk_attendance_student_date UNIQUE (student_id, date),
    INDEX idx_attendance_class_date (class_id, date)
) ENGINE=InnoDB;

-- ============================================================================
-- 4. FINANCE
-- ============================================================================

CREATE TABLE fee_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_mandatory BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE fee_structures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    late_fee_amount NUMERIC(19, 2) DEFAULT 0,
    grace_period_days INT DEFAULT 0,
    INDEX idx_fee_structure_academic_year (academic_year)
) ENGINE=InnoDB;

CREATE TABLE fee_structure_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    structure_id BIGINT NOT NULL REFERENCES fee_structures(id),
    category_id BIGINT NOT NULL REFERENCES fee_categories(id),
    amount NUMERIC(19, 2) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE student_fee_maps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    structure_id BIGINT NOT NULL REFERENCES fee_structures(id),
    discount_amount NUMERIC(19, 2) DEFAULT 0,
    discount_reason VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_student_fee_map_student FOREIGN KEY (student_id) REFERENCES students(id),
    INDEX idx_student_fee_map_student (student_id)
) ENGINE=InnoDB;

CREATE TABLE payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT NOW(),
    payment_method VARCHAR(20) NOT NULL,
    gateway_txn_id VARCHAR(100) UNIQUE,
    status VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL DEFAULT 'FEE_PAYMENT',
    receipt_no VARCHAR(50) UNIQUE,
    CONSTRAINT fk_payment_txn_student FOREIGN KEY (student_id) REFERENCES students(id),
    INDEX idx_payment_txn_student (student_id),
    INDEX idx_payment_txn_date (payment_date)
) ENGINE=InnoDB;

CREATE TABLE receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE REFERENCES payment_transactions(id),
    receipt_no VARCHAR(50) NOT NULL UNIQUE,
    file_url VARCHAR(255),
    generated_at TIMESTAMP DEFAULT NOW()
) ENGINE=InnoDB;

-- ============================================================================
-- 5. TRANSPORT
-- ============================================================================

CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type VARCHAR(20),
    capacity INT,
    make_model VARCHAR(100),
    registration_year INT,
    driver_name VARCHAR(100),
    driver_phone VARCHAR(20),
    gps_device_id VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vehicle_number (vehicle_number)
) ENGINE=InnoDB;

CREATE TABLE routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_name VARCHAR(100) NOT NULL,
    route_code VARCHAR(20),
    vehicle_id BIGINT,
    route_type VARCHAR(20),
    start_location VARCHAR(200),
    end_location VARCHAR(200),
    estimated_duration_minutes INT,
    distance_km DECIMAL(10,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_route_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE stops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    stop_name VARCHAR(200) NOT NULL,
    stop_order INT NOT NULL,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    scheduled_arrival_time TIME,
    landmark VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stop_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    INDEX idx_stop_route (route_id, stop_order)
) ENGINE=InnoDB;

CREATE TABLE trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    trip_date DATE NOT NULL,
    trip_type VARCHAR(20) NOT NULL,
    scheduled_start_time TIME,
    actual_start_time TIME,
    scheduled_end_time TIME,
    actual_end_time TIME,
    status VARCHAR(20) NOT NULL,
    driver_name VARCHAR(100),
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    INDEX idx_trip_route_date (route_id, trip_date)
) ENGINE=InnoDB;

CREATE TABLE student_transport_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    stop_id BIGINT NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    assignment_type VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_stop FOREIGN KEY (stop_id) REFERENCES stops(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE location_pings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    trip_id BIGINT,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    speed_kmh DECIMAL(5,2),
    heading DECIMAL(5,2),
    accuracy_meters DECIMAL(6,2),
    captured_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL,
    device_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ping_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    CONSTRAINT fk_ping_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    INDEX idx_ping_vehicle_received (vehicle_id, received_at DESC)
) ENGINE=InnoDB;

CREATE TABLE transport_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    stop_id BIGINT,
    event_type VARCHAR(20) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    recorded_by VARCHAR(100),
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_stop FOREIGN KEY (stop_id) REFERENCES stops(id) ON DELETE SET NULL,
    INDEX idx_event_trip_student (trip_id, student_id)
) ENGINE=InnoDB;

-- ============================================================================
-- 6. COMMUNICATION & AUDIT
-- ============================================================================

CREATE TABLE notification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    platform VARCHAR(20) NOT NULL,
    device_id VARCHAR(100) NULL,
    device_model VARCHAR(100) NULL,
    app_version VARCHAR(20) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_token_user_active (user_id, is_active)
) ENGINE=InnoDB;

CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data_payload TEXT NULL,
    target_token VARCHAR(500) NULL,
    platform VARCHAR(20) NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    delivered_at TIMESTAMP NULL,
    read_at TIMESTAMP NULL,
    fcm_message_id VARCHAR(255) NULL,
    error_message TEXT NULL,
    reference_id VARCHAR(100) NULL,
    reference_type VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_log_user_sent (user_id, sent_at DESC)
) ENGINE=InnoDB;

CREATE TABLE notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    publish_date DATE NOT NULL,
    expiry_date DATE NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    target_role VARCHAR(30) NULL,
    target_class_ids JSON NULL,
    attachment_urls JSON NULL,
    attachment_url VARCHAR(500) NULL,
    created_by_user_id BINARY(16) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notice_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_notices_active_publish (is_active, publish_date DESC)
) ENGINE=InnoDB;

CREATE TABLE notice_read_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    notice_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notice_read_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notice_read_notice FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_notice UNIQUE (user_id, notice_id)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NULL,
    resource_id VARCHAR(100) NULL,
    details TEXT NULL,
    ip_address VARCHAR(50) NULL,
    user_agent VARCHAR(200) NULL,
    status VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_audit_created_at (created_at DESC)
) ENGINE=InnoDB;
