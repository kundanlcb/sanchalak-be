package com.cm.sanchalak.service;

import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassRepository classRepository;
    private final AttendanceRepository attendanceRepository;
    private final AuditLogRepository auditLogRepository;
    private final StudentMarksRepository studentMarksRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalStudents = studentRepository.countByDeletedFalse();

        stats.put("students", totalStudents);
        stats.put("teachers", teacherRepository.countByDeletedFalse());
        stats.put("classes", classRepository.count());

        // Attendance Calculation
        if (totalStudents > 0) {
            long present = attendanceRepository.countByDateAndStatus(LocalDate.now(), AttendanceStatus.PRESENT);
            // Simple percentage
            double percent = ((double) present / totalStudents) * 100;
            stats.put("attendance", Math.round(percent)); // Store as Long (e.g., 95)
        } else {
            stats.put("attendance", 0L);
        }

        return stats;
    }

    public Map<String, Long> getGenderDistribution() {
        long male = studentRepository.countByGender("MALE");
        long female = studentRepository.countByGender("FEMALE");
        long total = studentRepository.count();
        long other = total - male - female;

        Map<String, Long> dist = new HashMap<>();
        dist.put("MALE", male);
        dist.put("FEMALE", female);
        if (other > 0) {
            dist.put("OTHER", other);
        }
        return dist;
    }

    public List<Map<String, Object>> getTeacherPerformance() {
        return studentMarksRepository.findTeacherPerformance();
    }

    public List<Map<String, Object>> getActivityFeed() {
        List<AuditLog> recentLogs = auditLogRepository.findTop10ByOrderByCreatedAtDesc();

        if (recentLogs.isEmpty()) {
            return new ArrayList<>();
        }

        return recentLogs.stream().map(log -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("message", formatLogMessage(log));
            item.put("timestamp", log.getCreatedAt());
            item.put("type", log.getStatus() != null && log.getStatus().equals("FAILURE") ? "ALERT" : "INFO");
            return item;
        }).collect(Collectors.toList());
    }

    private String formatLogMessage(AuditLog log) {
        String action = log.getActionType();
        String resource = log.getResourceType();

        if ("AUTH_SUCCESS".equals(action))
            return "User login successful";
        if ("AUTH_FAILURE".equals(action))
            return "Failed login attempt";
        if ("PAYMENT_SUCCESS".equals(action))
            return "Payment received for " + log.getResourceId();

        return action + " on " + resource + (log.getResourceId() != null ? ": " + log.getResourceId() : "");
    }
}
