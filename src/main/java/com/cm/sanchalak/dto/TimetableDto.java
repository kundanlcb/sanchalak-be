package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Timetable DTO for student/parent portal
 * Shows weekly class schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableDto {
    
    private Long studentId;
    
    private String className;
    
    private String academicYear;
    
    private Map<String, List<PeriodDto>> weeklySchedule; // Key: DAY_OF_WEEK (MONDAY, TUESDAY, etc.)
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodDto {
        
        private Integer periodNumber;
        
        private String startTime; // HH:mm format
        
        private String endTime; // HH:mm format
        
        private String subjectName;
        
        private String teacherName;
        
        private String roomNumber;
        
        private String className; // For teachers to know which class
        
        private String periodType; // LECTURE, LAB, LIBRARY, SPORTS, BREAK
    }
}
