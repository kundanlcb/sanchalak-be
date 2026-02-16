package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard data DTO for student/parent home screen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    private AttendanceSummary attendanceSummary;
    private HomeworkSummary homeworkSummary;
    private UpcomingExam nextExam;
    private FeesSummary feesSummary;
    private List<RecentNotice> recentNotices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceSummary {
        private Integer presentDays;
        private Integer absentDays;
        private Integer totalDays;
        private Double attendancePercentage;
        private LocalDate lastMarkedDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HomeworkSummary {
        private Integer pendingCount;
        private Integer submittedCount;
        private Integer lateCount;
        private LocalDateTime nextDueDate;
        private String nextDueSubject;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpcomingExam {
        private String examName;
        private String subject;
        private LocalDate examDate;
        private Integer daysRemaining;
        private String syllabus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeesSummary {
        private Double totalDue;
        private Double totalPaid;
        private LocalDate nextDueDate;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentNotice {
        private Long noticeId;
        private String title;
        private String description;
        private LocalDate publishDate;
        private String priority;
        private Boolean isRead;
    }
}