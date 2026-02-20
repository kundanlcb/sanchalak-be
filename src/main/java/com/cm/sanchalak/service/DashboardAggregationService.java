package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.DashboardDto;
import com.cm.sanchalak.entity.AttendanceRecord;
import com.cm.sanchalak.entity.ExamSchedule;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service to aggregate dashboard data from multiple sources
 */
@Service
@RequiredArgsConstructor
public class DashboardAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardAggregationService.class);

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final StudentFeeMapRepository studentFeeMapRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeReadStatusRepository noticeReadStatusRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public DashboardDto getDashboardForStudentByUser(UUID userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for user: " + userId));
        return getDashboardForStudent(student.getId());
    }

    /**
     * Get dashboard data for student
     */
    @Transactional(readOnly = true)
    public DashboardDto getDashboardForStudent(Long studentId) {
        logger.info("Fetching dashboard data for student: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Long classId = student.getStudentClass() != null ? student.getStudentClass().getId() : null;
        UUID userId = student.getUserId();

        return DashboardDto.builder()
                .attendanceSummary(getAttendanceSummary(studentId))
                .homeworkSummary(getHomeworkSummary(studentId, classId))
                .nextExam(getUpcomingExam(classId))
                .feesSummary(getFeesSummary(studentId))
                .recentNotices(getRecentNotices(userId, "STUDENT"))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardDto getDashboardForParentByUser(UUID userId) {
        // In the future, this might involve deeper parent-student relationship logic
        return getDashboardForParent(userId);
    }

    /**
     * Get dashboard data for parent (aggregates data from all children)
     */
    @Transactional(readOnly = true)
    public DashboardDto getDashboardForParent(UUID parentId) {
        logger.info("Fetching dashboard data for parent: {}", parentId);

        return DashboardDto.builder()
                .recentNotices(getRecentNotices(parentId, "PARENT"))
                .build();
    }

    /**
     * Get dashboard data for teacher
     */
    @Transactional(readOnly = true)
    public DashboardDto getDashboardForTeacher(UUID userId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        logger.info("Fetching dashboard data for teacher: {} ({})", userId, teacher.getName());

        return DashboardDto.builder()
                .recentNotices(getRecentNotices(userId, "TEACHER"))
                .build();
    }

    /**
     * Get attendance summary for student
     */
    private DashboardDto.AttendanceSummary getAttendanceSummary(Long studentId) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfYear(1); // Assume Jan 1st for now

        var records = attendanceRepository.findByStudentIdAndDateBetween(studentId, startDate, now);

        int total = records.size();
        int present = (int) records.stream()
                .filter(r -> r.getStatus() != null && "PRESENT".equalsIgnoreCase(r.getStatus().name()))
                .count();
        int absent = (int) records.stream()
                .filter(r -> r.getStatus() != null && "ABSENT".equalsIgnoreCase(r.getStatus().name()))
                .count();

        LocalDate lastMarked = records.stream()
                .map(AttendanceRecord::getDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return DashboardDto.AttendanceSummary.builder()
                .presentDays(present)
                .absentDays(absent)
                .totalDays(total)
                .attendancePercentage(total > 0 ? (double) present / total * 100 : 0.0)
                .lastMarkedDate(lastMarked)
                .build();
    }

    /**
     * Get homework summary for student
     */
    private DashboardDto.HomeworkSummary getHomeworkSummary(Long studentId, Long classId) {
        if (classId == null)
            return null;

        var allHomework = homeworkRepository.findByStudentClassId(classId);

        int submitted = 0;
        int pending = 0;
        int late = 0;

        LocalDate now = LocalDate.now();
        LocalDate nextDueDate = null;
        String nextDueSubject = null;

        for (var hw : allHomework) {
            boolean isSubmitted = homeworkSubmissionRepository.existsByHomeworkIdAndStudentId(hw.getId(), studentId);
            if (isSubmitted) {
                submitted++;
            } else {
                pending++;
                if (hw.getDueDate().isBefore(now)) {
                    late++;
                }
                if (nextDueDate == null || hw.getDueDate().isBefore(nextDueDate)) {
                    if (hw.getDueDate().isAfter(now) || hw.getDueDate().isEqual(now)) {
                        nextDueDate = hw.getDueDate();
                        nextDueSubject = hw.getSubject() != null ? hw.getSubject().getName() : "Homework";
                    }
                }
            }
        }

        return DashboardDto.HomeworkSummary.builder()
                .pendingCount(pending)
                .submittedCount(submitted)
                .lateCount(late)
                .nextDueDate(nextDueDate != null ? nextDueDate.atStartOfDay() : null)
                .nextDueSubject(nextDueSubject)
                .build();
    }

    /**
     * Get next upcoming exam for student
     */
    private DashboardDto.UpcomingExam getUpcomingExam(Long classId) {
        if (classId == null)
            return null;

        LocalDate now = LocalDate.now();
        var upcomingExams = examScheduleRepository.findByStudentClassIdAndExamDateBetween(classId, now,
                now.plusMonths(1));

        return upcomingExams.stream()
                .min(Comparator.comparing(ExamSchedule::getExamDate))
                .map(e -> DashboardDto.UpcomingExam.builder()
                        .examName(e.getExamTerm() != null ? e.getExamTerm().getName() : "Exam")
                        .subject(e.getSubject() != null ? e.getSubject().getName() : "N/A")
                        .examDate(e.getExamDate())
                        .daysRemaining((int) ChronoUnit.DAYS.between(now, e.getExamDate()))
                        .build())
                .orElse(null);
    }

    /**
     * Get fees summary for student
     */
    private DashboardDto.FeesSummary getFeesSummary(Long studentId) {
        var feeMaps = studentFeeMapRepository.findByStudentId(studentId);

        double totalDue = 0.0;
        for (var map : feeMaps) {
            var structure = map.getFeeStructure();
            if (structure != null && structure.getItems() != null) {
                double amount = structure.getItems().stream()
                        .mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0)
                        .sum();
                double discount = map.getDiscountAmount() != null ? map.getDiscountAmount().doubleValue() : 0.0;
                totalDue += (amount - discount);
            }
        }

        return DashboardDto.FeesSummary.builder()
                .totalDue(totalDue)
                .totalPaid(0.0)
                .currency("INR")
                .build();
    }

    /**
     * Get recent notices for user
     */
    private java.util.List<DashboardDto.RecentNotice> getRecentNotices(UUID userId, String role) {
        LocalDate now = LocalDate.now();
        var notices = noticeRepository.findRecentByTargetRole(role, now.minusDays(30));

        return notices.stream()
                .limit(5)
                .map(n -> DashboardDto.RecentNotice.builder()
                        .noticeId(n.getId())
                        .title(n.getTitle())
                        .description(n.getContent())
                        .publishDate(n.getPublishDate())
                        .priority(n.getPriority())
                        .isRead(userId != null
                                && noticeReadStatusRepository.existsByUserIdAndNoticeId(userId, n.getId()))
                        .build())
                .collect(Collectors.toList());
    }
}
