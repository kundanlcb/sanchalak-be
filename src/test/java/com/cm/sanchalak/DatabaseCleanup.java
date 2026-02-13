package com.cm.sanchalak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleanup {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public void cleanAllTables() {
        // Disable referential integrity
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        
        // Get all tables and truncate
        jdbcTemplate.execute("TRUNCATE TABLE attendance_records");
        jdbcTemplate.execute("TRUNCATE TABLE payment_transactions");
        jdbcTemplate.execute("TRUNCATE TABLE student_fee_maps");
        jdbcTemplate.execute("TRUNCATE TABLE student_marks");
        jdbcTemplate.execute("TRUNCATE TABLE homework_submissions");
        jdbcTemplate.execute("TRUNCATE TABLE parent_student_links");
        jdbcTemplate.execute("TRUNCATE TABLE student_transport_assignments");
        jdbcTemplate.execute("TRUNCATE TABLE transport_events");
        jdbcTemplate.execute("TRUNCATE TABLE students");
        jdbcTemplate.execute("TRUNCATE TABLE classes");
        jdbcTemplate.execute("TRUNCATE TABLE teacher_specializations");
        jdbcTemplate.execute("TRUNCATE TABLE teachers");
        jdbcTemplate.execute("TRUNCATE TABLE class_routines");
        jdbcTemplate.execute("TRUNCATE TABLE exam_schedules");
        jdbcTemplate.execute("TRUNCATE TABLE exam_terms");
        jdbcTemplate.execute("TRUNCATE TABLE class_subjects");
        jdbcTemplate.execute("TRUNCATE TABLE subjects");
        jdbcTemplate.execute("TRUNCATE TABLE user_roles");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("TRUNCATE TABLE otp_verifications");
        // Don't truncate roles as they are needed
        
        // Re-enable referential integrity
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        
        // Verify critical tables are empty to avoid race conditions/cache issues
        Integer otpCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM otp_verifications", Integer.class);
        if (otpCount != null && otpCount > 0) {
            throw new RuntimeException("Failed to truncate otp_verifications. Count: " + otpCount);
        }
    }
    
    public void cleanOtpRecords() {
        // Only clean OTP-related records for rate limit reset
        jdbcTemplate.execute("TRUNCATE TABLE otp_verifications");
    }
}
