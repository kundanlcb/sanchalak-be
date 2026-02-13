package com.cm.sanchalak.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard data DTO for student/parent home screen
 */
public class DashboardDto {
    
    private AttendanceSummary attendanceSummary;
    private HomeworkSummary homeworkSummary;
    private UpcomingExam nextExam;
    private FeesSummary feesSummary;
    private List<RecentNotice> recentNotices;

    public DashboardDto() {}

    public DashboardDto(AttendanceSummary attendanceSummary, HomeworkSummary homeworkSummary, UpcomingExam nextExam, FeesSummary feesSummary, List<RecentNotice> recentNotices) {
        this.attendanceSummary = attendanceSummary;
        this.homeworkSummary = homeworkSummary;
        this.nextExam = nextExam;
        this.feesSummary = feesSummary;
        this.recentNotices = recentNotices;
    }

    public static DashboardDtoBuilder builder() {
        return new DashboardDtoBuilder();
    }

    public AttendanceSummary getAttendanceSummary() { return attendanceSummary; }
    public HomeworkSummary getHomeworkSummary() { return homeworkSummary; }
    public UpcomingExam getNextExam() { return nextExam; }
    public FeesSummary getFeesSummary() { return feesSummary; }
    public List<RecentNotice> getRecentNotices() { return recentNotices; }

    public void setAttendanceSummary(AttendanceSummary attendanceSummary) { this.attendanceSummary = attendanceSummary; }
    public void setHomeworkSummary(HomeworkSummary homeworkSummary) { this.homeworkSummary = homeworkSummary; }
    public void setNextExam(UpcomingExam nextExam) { this.nextExam = nextExam; }
    public void setFeesSummary(FeesSummary feesSummary) { this.feesSummary = feesSummary; }
    public void setRecentNotices(List<RecentNotice> recentNotices) { this.recentNotices = recentNotices; }

    public static class DashboardDtoBuilder {
        private AttendanceSummary attendanceSummary;
        private HomeworkSummary homeworkSummary;
        private UpcomingExam nextExam;
        private FeesSummary feesSummary;
        private List<RecentNotice> recentNotices;

        DashboardDtoBuilder() {}

        public DashboardDtoBuilder attendanceSummary(AttendanceSummary attendanceSummary) { this.attendanceSummary = attendanceSummary; return this; }
        public DashboardDtoBuilder homeworkSummary(HomeworkSummary homeworkSummary) { this.homeworkSummary = homeworkSummary; return this; }
        public DashboardDtoBuilder nextExam(UpcomingExam nextExam) { this.nextExam = nextExam; return this; }
        public DashboardDtoBuilder feesSummary(FeesSummary feesSummary) { this.feesSummary = feesSummary; return this; }
        public DashboardDtoBuilder recentNotices(List<RecentNotice> recentNotices) { this.recentNotices = recentNotices; return this; }

        public DashboardDto build() {
            return new DashboardDto(attendanceSummary, homeworkSummary, nextExam, feesSummary, recentNotices);
        }
    }
    
    public static class AttendanceSummary {
        private Integer presentDays;
        private Integer absentDays;
        private Integer totalDays;
        private Double attendancePercentage;
        private LocalDate lastMarkedDate;

        public AttendanceSummary() {}

        public AttendanceSummary(Integer presentDays, Integer absentDays, Integer totalDays, Double attendancePercentage, LocalDate lastMarkedDate) {
            this.presentDays = presentDays;
            this.absentDays = absentDays;
            this.totalDays = totalDays;
            this.attendancePercentage = attendancePercentage;
            this.lastMarkedDate = lastMarkedDate;
        }

        public static AttendanceSummaryBuilder builder() {
            return new AttendanceSummaryBuilder();
        }

        public Integer getPresentDays() { return presentDays; }
        public Integer getAbsentDays() { return absentDays; }
        public Integer getTotalDays() { return totalDays; }
        public Double getAttendancePercentage() { return attendancePercentage; }
        public LocalDate getLastMarkedDate() { return lastMarkedDate; }

        public void setPresentDays(Integer presentDays) { this.presentDays = presentDays; }
        public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }
        public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
        public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
        public void setLastMarkedDate(LocalDate lastMarkedDate) { this.lastMarkedDate = lastMarkedDate; }

        public static class AttendanceSummaryBuilder {
            private Integer presentDays;
            private Integer absentDays;
            private Integer totalDays;
            private Double attendancePercentage;
            private LocalDate lastMarkedDate;

            AttendanceSummaryBuilder() {}

            public AttendanceSummaryBuilder presentDays(Integer presentDays) { this.presentDays = presentDays; return this; }
            public AttendanceSummaryBuilder absentDays(Integer absentDays) { this.absentDays = absentDays; return this; }
            public AttendanceSummaryBuilder totalDays(Integer totalDays) { this.totalDays = totalDays; return this; }
            public AttendanceSummaryBuilder attendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; return this; }
            public AttendanceSummaryBuilder lastMarkedDate(LocalDate lastMarkedDate) { this.lastMarkedDate = lastMarkedDate; return this; }

            public AttendanceSummary build() {
                return new AttendanceSummary(presentDays, absentDays, totalDays, attendancePercentage, lastMarkedDate);
            }
        }
    }
    
    public static class HomeworkSummary {
        private Integer pendingCount;
        private Integer submittedCount;
        private Integer lateCount;
        private LocalDateTime nextDueDate;
        private String nextDueSubject;

        public HomeworkSummary() {}

        public HomeworkSummary(Integer pendingCount, Integer submittedCount, Integer lateCount, LocalDateTime nextDueDate, String nextDueSubject) {
            this.pendingCount = pendingCount;
            this.submittedCount = submittedCount;
            this.lateCount = lateCount;
            this.nextDueDate = nextDueDate;
            this.nextDueSubject = nextDueSubject;
        }

        public static HomeworkSummaryBuilder builder() {
            return new HomeworkSummaryBuilder();
        }

        public Integer getPendingCount() { return pendingCount; }
        public Integer getSubmittedCount() { return submittedCount; }
        public Integer getLateCount() { return lateCount; }
        public LocalDateTime getNextDueDate() { return nextDueDate; }
        public String getNextDueSubject() { return nextDueSubject; }

        public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
        public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }
        public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }
        public void setNextDueDate(LocalDateTime nextDueDate) { this.nextDueDate = nextDueDate; }
        public void setNextDueSubject(String nextDueSubject) { this.nextDueSubject = nextDueSubject; }

        public static class HomeworkSummaryBuilder {
            private Integer pendingCount;
            private Integer submittedCount;
            private Integer lateCount;
            private LocalDateTime nextDueDate;
            private String nextDueSubject;

            HomeworkSummaryBuilder() {}

            public HomeworkSummaryBuilder pendingCount(Integer pendingCount) { this.pendingCount = pendingCount; return this; }
            public HomeworkSummaryBuilder submittedCount(Integer submittedCount) { this.submittedCount = submittedCount; return this; }
            public HomeworkSummaryBuilder lateCount(Integer lateCount) { this.lateCount = lateCount; return this; }
            public HomeworkSummaryBuilder nextDueDate(LocalDateTime nextDueDate) { this.nextDueDate = nextDueDate; return this; }
            public HomeworkSummaryBuilder nextDueSubject(String nextDueSubject) { this.nextDueSubject = nextDueSubject; return this; }

            public HomeworkSummary build() {
                return new HomeworkSummary(pendingCount, submittedCount, lateCount, nextDueDate, nextDueSubject);
            }
        }
    }
    
    public static class UpcomingExam {
        private String examName;
        private String subject;
        private LocalDate examDate;
        private Integer daysRemaining;
        private String syllabus;

        public UpcomingExam() {}

        public UpcomingExam(String examName, String subject, LocalDate examDate, Integer daysRemaining, String syllabus) {
            this.examName = examName;
            this.subject = subject;
            this.examDate = examDate;
            this.daysRemaining = daysRemaining;
            this.syllabus = syllabus;
        }

        public static UpcomingExamBuilder builder() {
            return new UpcomingExamBuilder();
        }

        public String getExamName() { return examName; }
        public String getSubject() { return subject; }
        public LocalDate getExamDate() { return examDate; }
        public Integer getDaysRemaining() { return daysRemaining; }
        public String getSyllabus() { return syllabus; }

        public void setExamName(String examName) { this.examName = examName; }
        public void setSubject(String subject) { this.subject = subject; }
        public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
        public void setDaysRemaining(Integer daysRemaining) { this.daysRemaining = daysRemaining; }
        public void setSyllabus(String syllabus) { this.syllabus = syllabus; }

        public static class UpcomingExamBuilder {
            private String examName;
            private String subject;
            private LocalDate examDate;
            private Integer daysRemaining;
            private String syllabus;

            UpcomingExamBuilder() {}

            public UpcomingExamBuilder examName(String examName) { this.examName = examName; return this; }
            public UpcomingExamBuilder subject(String subject) { this.subject = subject; return this; }
            public UpcomingExamBuilder examDate(LocalDate examDate) { this.examDate = examDate; return this; }
            public UpcomingExamBuilder daysRemaining(Integer daysRemaining) { this.daysRemaining = daysRemaining; return this; }
            public UpcomingExamBuilder syllabus(String syllabus) { this.syllabus = syllabus; return this; }

            public UpcomingExam build() {
                return new UpcomingExam(examName, subject, examDate, daysRemaining, syllabus);
            }
        }
    }
    
    public static class FeesSummary {
        private Double totalDue;
        private Double totalPaid;
        private LocalDate nextDueDate;
        private String currency;

        public FeesSummary() {}

        public FeesSummary(Double totalDue, Double totalPaid, LocalDate nextDueDate, String currency) {
            this.totalDue = totalDue;
            this.totalPaid = totalPaid;
            this.nextDueDate = nextDueDate;
            this.currency = currency;
        }

        public static FeesSummaryBuilder builder() {
            return new FeesSummaryBuilder();
        }

        public Double getTotalDue() { return totalDue; }
        public Double getTotalPaid() { return totalPaid; }
        public LocalDate getNextDueDate() { return nextDueDate; }
        public String getCurrency() { return currency; }

        public void setTotalDue(Double totalDue) { this.totalDue = totalDue; }
        public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }
        public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
        public void setCurrency(String currency) { this.currency = currency; }

        public static class FeesSummaryBuilder {
            private Double totalDue;
            private Double totalPaid;
            private LocalDate nextDueDate;
            private String currency;

            FeesSummaryBuilder() {}

            public FeesSummaryBuilder totalDue(Double totalDue) { this.totalDue = totalDue; return this; }
            public FeesSummaryBuilder totalPaid(Double totalPaid) { this.totalPaid = totalPaid; return this; }
            public FeesSummaryBuilder nextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; return this; }
            public FeesSummaryBuilder currency(String currency) { this.currency = currency; return this; }

            public FeesSummary build() {
                return new FeesSummary(totalDue, totalPaid, nextDueDate, currency);
            }
        }
    }
    
    public static class RecentNotice {
        private Long noticeId;
        private String title;
        private String description;
        private LocalDate publishDate;
        private String priority;
        private Boolean isRead;

        public RecentNotice() {}

        public RecentNotice(Long noticeId, String title, String description, LocalDate publishDate, String priority, Boolean isRead) {
            this.noticeId = noticeId;
            this.title = title;
            this.description = description;
            this.publishDate = publishDate;
            this.priority = priority;
            this.isRead = isRead;
        }

        public static RecentNoticeBuilder builder() {
            return new RecentNoticeBuilder();
        }

        public Long getNoticeId() { return noticeId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDate getPublishDate() { return publishDate; }
        public String getPriority() { return priority; }
        public Boolean getIsRead() { return isRead; }

        public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }
        public void setPriority(String priority) { this.priority = priority; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }

        public static class RecentNoticeBuilder {
            private Long noticeId;
            private String title;
            private String description;
            private LocalDate publishDate;
            private String priority;
            private Boolean isRead;

            RecentNoticeBuilder() {}

            public RecentNoticeBuilder noticeId(Long noticeId) { this.noticeId = noticeId; return this; }
            public RecentNoticeBuilder title(String title) { this.title = title; return this; }
            public RecentNoticeBuilder description(String description) { this.description = description; return this; }
            public RecentNoticeBuilder publishDate(LocalDate publishDate) { this.publishDate = publishDate; return this; }
            public RecentNoticeBuilder priority(String priority) { this.priority = priority; return this; }
            public RecentNoticeBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }

            public RecentNotice build() {
                return new RecentNotice(noticeId, title, description, publishDate, priority, isRead);
            }
        }
    }
}