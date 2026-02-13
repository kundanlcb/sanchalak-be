-- V5__finance_schema.sql

-- 1. Fee Categories (e.g., Tuition, Transport)
CREATE TABLE fee_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_mandatory BOOLEAN DEFAULT TRUE
);

-- 2. Fee Structures (e.g., Class 10 - 2026-27)
CREATE TABLE fee_structures (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    frequency VARCHAR(20) NOT NULL, -- ENUM: MONTHLY, QUARTERLY, ANNUAL
    late_fee_amount NUMERIC(19, 2) DEFAULT 0,
    grace_period_days INT DEFAULT 0
);

-- 3. Fee Structure Items (Mapping Category to Structure with Amount)
CREATE TABLE fee_structure_items (
    id BIGSERIAL PRIMARY KEY,
    structure_id BIGINT NOT NULL REFERENCES fee_structures(id),
    category_id BIGINT NOT NULL REFERENCES fee_categories(id),
    amount NUMERIC(19, 2) NOT NULL
);

-- 4. Student Fee Maps (Assigning a Structure to a Student)
CREATE TABLE student_fee_maps (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL, -- Logical FK to students table (assumed existing)
    structure_id BIGINT NOT NULL REFERENCES fee_structures(id),
    discount_amount NUMERIC(19, 2) DEFAULT 0,
    discount_reason VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_student_fee_map_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 5. Payment Transactions
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT NOW(),
    payment_method VARCHAR(20) NOT NULL, -- CASH, UPI, CARD, CHEQUE
    gateway_txn_id VARCHAR(100) UNIQUE, -- Idempotency Key
    status VARCHAR(20) NOT NULL, -- SUCCESS, PENDING, FAILED, REFUNDED
    transaction_type VARCHAR(20) NOT NULL DEFAULT 'FEE_PAYMENT', -- FEE_PAYMENT, REFUND
    receipt_no VARCHAR(50) UNIQUE,
    CONSTRAINT fk_payment_txn_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 6. Receipts
CREATE TABLE receipts (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE REFERENCES payment_transactions(id),
    receipt_no VARCHAR(50) NOT NULL UNIQUE,
    file_url VARCHAR(255),
    generated_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for Performance
CREATE INDEX idx_fee_structure_academic_year ON fee_structures(academic_year);
CREATE INDEX idx_student_fee_map_student ON student_fee_maps(student_id);
CREATE INDEX idx_payment_txn_student ON payment_transactions(student_id);
CREATE INDEX idx_payment_txn_date ON payment_transactions(payment_date);
