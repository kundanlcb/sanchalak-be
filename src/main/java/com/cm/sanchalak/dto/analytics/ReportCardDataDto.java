package com.cm.sanchalak.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportCardDataDto {
    private StudentProfileDto student;
    private TermDetailsDto term;
    private AttendanceSummaryDto attendance;
    private List<SubjectMarkDto> academics;
    private ResultSummaryDto result;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentProfileDto {
        private String name;
        private String excludeNumber; // rollNumber/admissionNumber
        private String className;
        private String section;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TermDetailsDto {
        private String name;
        private String session; // e.g. "2023-2024"
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResultSummaryDto {
        private Double totalScore;
        private Double totalMaxScore;
        private Double percentage;
        private String rank; // Optional
        private String finalGrade;
    }
}
