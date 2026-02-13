package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Academic results DTO for student/parent portal
 * Shows exam results and overall performance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultsDto {
    
    private Long studentId;
    
    private String studentName;
    
    private String className;
    
    private String academicYear;
    
    private List<ExamResultDto> examResults;
    
    private OverallPerformanceDto overallPerformance;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamResultDto {
        
        private Long examId;
        
        private String examName; // Mid-term, Final, Unit Test, etc.
        
        private String examDate;
        
        private List<SubjectScoreDto> subjectScores;
        
        private Double totalMarks;
        
        private Double obtainedMarks;
        
        private Double percentage;
        
        private String grade; // A+, A, B+, etc.
        
        private Integer classRank;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectScoreDto {
        
        private String subjectName;
        
        private Double maxMarks;
        
        private Double obtainedMarks;
        
        private String grade;
        
        private String remarks;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallPerformanceDto {
        
        private Double cumulativePercentage;
        
        private String overallGrade;
        
        private Integer classRank;
        
        private Integer totalStudents;
        
        private String performanceTrend; // IMPROVING, STABLE, DECLINING
    }
}
