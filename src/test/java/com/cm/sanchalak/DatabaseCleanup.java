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
        
        String[] tables = {
            "attendance_records", "payment_transactions", "student_fee_maps", "student_marks", 
            "homework_submissions", "parent_student_links", "student_transport_assignments", 
            "transport_events", "students", "classes", "teacher_specializations", "teachers", 
            "class_routines", "exam_schedules", "exam_terms", "class_subjects", "subjects", 
            "user_roles", "users", "otp_verifications"
        };
        
        for (String table : tables) {
            try {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            } catch (Exception e) {
                // Ignore table not found errors (H2 error code 42104 or message contains "not found")
                // Check cause because Spring wraps it in BadSqlGrammarException
                boolean isTableNotFound = false;
                Throwable cause = e;
                while (cause != null) {
                    String msg = cause.getMessage();
                    if (msg != null && (msg.toLowerCase().contains("not found") || msg.contains("42104"))) {
                        isTableNotFound = true;
                        break;
                    }
                    cause = cause.getCause();
                }
                
                if (isTableNotFound) {
                     // System.out.println("Ignored missing table: " + table);
                } else {
                    System.err.println("Failed to truncate table: " + table + ". Root cause: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                    throw new RuntimeException("Failed to truncate table: " + table, e);
                }
            }
        }
        
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
