package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.DashboardDto;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Service to aggregate dashboard data from multiple sources
 */
@Service
@RequiredArgsConstructor
public class DashboardAggregationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardAggregationService.class);
    
    private final StudentRepository studentRepository;
    
    // TODO: Inject repositories/services for attendance, homework, exams, fees, notices
    // private AttendanceRepository attendanceRepository;
    // private HomeworkRepository homeworkRepository;
    // private ExamRepository examRepository;
    // private FeeRepository feeRepository;
    // private NoticeRepository noticeRepository;
    
    /**
     * Get dashboard data for student
     */
    @Transactional(readOnly = true)
    public DashboardDto getDashboardForStudent(Long studentId) {
        logger.info("Fetching dashboard data for student: {}", studentId);
        
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found");
        }
        
        Student student = studentOpt.get();
        
        // Aggregate data from various sources
        DashboardDto.AttendanceSummary attendance = getAttendanceSummary(studentId);
        DashboardDto.HomeworkSummary homework = getHomeworkSummary(studentId);
        DashboardDto.UpcomingExam nextExam = getUpcomingExam(studentId);
        DashboardDto.FeesSummary fees = getFeesSummary(studentId);
        var recentNotices = getRecentNotices(student.getStudentClass() != null ? student.getStudentClass().getId() : null);
        
        return DashboardDto.builder()
            .attendanceSummary(attendance)
            .homeworkSummary(homework)
            .nextExam(nextExam)
            .feesSummary(fees)
            .recentNotices(recentNotices)
            .build();
    }
    
    /**
     * Get dashboard data for parent (aggregates data from all children)
     */
    @Transactional(readOnly = true)
    public DashboardDto getDashboardForParent(UUID parentId) {
        logger.info("Fetching dashboard data for parent: {}", parentId);
        
        // TODO: Implement parent dashboard aggregation
        // For now, return empty dashboard
        return DashboardDto.builder()
            .attendanceSummary(null)
            .homeworkSummary(null)
            .nextExam(null)
            .feesSummary(null)
            .recentNotices(new ArrayList<>())
            .build();
    }
    
    /**
     * Get attendance summary for student
     */
    private DashboardDto.AttendanceSummary getAttendanceSummary(Long studentId) {
        // TODO: Query attendance records for current academic year
        // For now, return mock data
        return DashboardDto.AttendanceSummary.builder()
            .presentDays(45)
            .absentDays(5)
            .totalDays(50)
            .attendancePercentage(90.0)
            .lastMarkedDate(LocalDate.now().minusDays(1))
            .build();
    }
    
    /**
     * Get homework summary for student
     */
    private DashboardDto.HomeworkSummary getHomeworkSummary(Long studentId) {
        // TODO: Query homework submissions and pending homework
        // For now, return mock data
        return DashboardDto.HomeworkSummary.builder()
            .pendingCount(3)
            .submittedCount(12)
            .lateCount(1)
            .nextDueDate(LocalDate.now().plusDays(2).atStartOfDay())
            .nextDueSubject("Mathematics")
            .build();
    }
    
    /**
     * Get next upcoming exam for student
     */
    private DashboardDto.UpcomingExam getUpcomingExam(Long studentId) {
        // TODO: Query exams table for next exam after today
        // For now, return mock data
        LocalDate examDate = LocalDate.now().plusDays(7);
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), examDate);
        
        return DashboardDto.UpcomingExam.builder()
            .examName("Mid-Term Exam")
            .subject("Science")
            .examDate(examDate)
            .daysRemaining((int) daysRemaining)
            .syllabus("Chapters 1-5")
            .build();
    }
    
    /**
     * Get fees summary for student
     */
    private DashboardDto.FeesSummary getFeesSummary(Long studentId) {
        // TODO: Query fees records for student
        // For now, return mock data
        return DashboardDto.FeesSummary.builder()
            .totalDue(5000.0)
            .totalPaid(15000.0)
            .nextDueDate(LocalDate.now().plusMonths(1))
            .currency("INR")
            .build();
    }
    
    /**
     * Get recent notices for class
     */
    private java.util.List<DashboardDto.RecentNotice> getRecentNotices(Long classId) {
        // TODO: Query notices for class and general notices
        // For now, return mock data
        return java.util.List.of(
            DashboardDto.RecentNotice.builder()
                .noticeId(1L)
                .title("Parent-Teacher Meeting")
                .description("PTM scheduled for next Saturday at 10 AM")
                .publishDate(LocalDate.now().minusDays(2))
                .priority("HIGH")
                .isRead(false)
                .build(),
            DashboardDto.RecentNotice.builder()
                .noticeId(2L)
                .title("Annual Sports Day")
                .description("Sports day on 15th Dec. All students must participate")
                .publishDate(LocalDate.now().minusDays(5))
                .priority("MEDIUM")
                .isRead(true)
                .build()
        );
    }
}
