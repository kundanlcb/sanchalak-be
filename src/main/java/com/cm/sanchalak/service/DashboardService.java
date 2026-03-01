package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.entity.AuditLog;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.repository.spec.BaseSpecification;
import com.cm.sanchalak.repository.spec.SchoolClassSpecification;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.repository.spec.TeacherSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassRepository classRepository;
    private final AttendanceRepository attendanceRepository;
    private final AuditLogRepository auditLogRepository;
    private final StudentMarksRepository studentMarksRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final OwnershipValidator ownership;

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        log.info("Dashboard stats - isPlatformAdmin: {}, schoolId: {}", SchoolContext.isPlatformAdmin(),
                SchoolContext.getSchoolId());

        long totalStudents = studentRepository.count(StudentSpecification.activeScoped());
        long totalTeachers = teacherRepository.count(TeacherSpecification.activeScoped());
        long totalClasses = classRepository.count(SchoolClassSpecification.activeScoped());

        log.info("Dashboard stats - students: {}, teachers: {}, classes: {}", totalStudents, totalTeachers,
                totalClasses);
        long rawTeacherCount = teacherRepository.count();
        log.info("Dashboard stats - raw teacher count (no filter): {}", rawTeacherCount);

        stats.put("students", totalStudents);
        stats.put("teachers", totalTeachers);
        stats.put("classes", totalClasses);

        java.time.LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        java.math.BigDecimal earnings = paymentRepository.sumCollectedSince(SchoolContext.getSchoolId(), startOfMonth);
        stats.put("monthlyEarnings", earnings != null ? earnings.longValue() : 0L);

        if (totalStudents > 0) {
            long present = attendanceRepository
                    .count((root, query, cb) -> {
                        java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
                        predicates.add(cb.equal(root.get("date"), LocalDate.now()));
                        predicates.add(cb.equal(root.get("status"), AttendanceStatus.PRESENT));
                        if (!SchoolContext.isPlatformAdmin()) {
                            predicates.add(cb.equal(root.join("student").get("schoolId"), SchoolContext.getSchoolId()));
                        }
                        return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                    });

            double percent = ((double) present / totalStudents) * 100;
            stats.put("attendance", Math.round(percent));
        } else {
            stats.put("attendance", 0L);
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getGenderDistribution() {
        long male = studentRepository.count(StudentSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("gender"), "MALE")));
        long female = studentRepository.count(StudentSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("gender"), "FEMALE")));
        long total = studentRepository.count(StudentSpecification.activeScoped());
        long other = total - male - female;

        Map<String, Long> dist = new HashMap<>();
        dist.put("MALE", male);
        dist.put("FEMALE", female);
        if (other > 0) {
            dist.put("OTHER", other);
        }
        return dist;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTeacherPerformance() {
        // Note: findTeacherPerformance uses a native query and might need schoolId
        // injection
        // if the native query doesn't handle it. Current implementation in repository
        // doesn't.
        // For now, it might return all schools' data if not fixed.
        // Fixing native query in repository or converting to Specification/JPQL is
        // better.
        return studentMarksRepository.findTeacherPerformance();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActivityFeed() {
        UUID schoolId = SchoolContext.getSchoolId();

        // AuditLog repository needs Specification support or schoolId filtering
        List<AuditLog> recentLogs = auditLogRepository.findAll(BaseSpecification.<AuditLog>scoped(),
                org.springframework.data.domain.PageRequest.of(0, 10,
                        org.springframework.data.domain.Sort.by("createdAt").descending()))
                .getContent();

        return recentLogs.stream().map(log -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("message", formatLogMessage(log));
            item.put("timestamp", log.getCreatedAt());
            item.put("type", "FAILURE".equals(log.getStatus()) ? "ALERT" : "INFO");
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStarStudents() {
        return studentMarksRepository.findTopStudentsByPerformance(SchoolContext.getSchoolId(),
                org.springframework.data.domain.PageRequest.of(0, 5));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getGrowthData(String duration) {
        UUID schoolId = SchoolContext.getSchoolId();
        int monthsToSubtract = 6;
        if ("1M".equals(duration))
            monthsToSubtract = 1;
        else if ("3M".equals(duration))
            monthsToSubtract = 3;

        java.time.LocalDateTime start = LocalDate.now().minusMonths(monthsToSubtract).withDayOfMonth(1).atStartOfDay();
        java.time.LocalDateTime end = java.time.LocalDateTime.now();

        List<com.cm.sanchalak.dto.analytics.CollectionTrendDto> feeTrends = paymentRepository.findCollectionTrend(start,
                end, schoolId);
        List<Map<String, Object>> admissionTrends = studentRepository.findAdmissionTrend(schoolId, start.toLocalDate());

        Map<String, Map<String, Object>> aggregated = new java.util.LinkedHashMap<>();
        LocalDate current = start.toLocalDate();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM");

        while (!current.isAfter(LocalDate.now())) {
            String monthName = current.format(formatter);
            if (!aggregated.containsKey(monthName)) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", monthName);
                map.put("collections", 0L);
                map.put("students", 0L);
                aggregated.put(monthName, map);
            }
            current = current.plusMonths(1);
        }

        for (com.cm.sanchalak.dto.analytics.CollectionTrendDto ft : feeTrends) {
            String monthName = ft.getDate().format(formatter);
            if (aggregated.containsKey(monthName)) {
                Map<String, Object> map = aggregated.get(monthName);
                long currentColl = ((Number) map.get("collections")).longValue();
                map.put("collections", currentColl + ft.getAmount().longValue());
            }
        }

        for (Map<String, Object> at : admissionTrends) {
            LocalDate date = (LocalDate) at.get("date");
            String monthName = date.format(formatter);
            if (aggregated.containsKey(monthName)) {
                Map<String, Object> map = aggregated.get(monthName);
                long count = ((Number) at.get("count")).longValue();
                map.put("students", ((Number) map.get("students")).longValue() + count);
            }
        }

        return new ArrayList<>(aggregated.values());
    }
}
