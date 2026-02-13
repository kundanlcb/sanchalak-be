package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Homework list DTO for student/parent portal
 * Shows pending and completed homework
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkListDto {
    
    private List<HomeworkItemDto> pending;
    
    private List<HomeworkItemDto> completed;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomeworkItemDto {
        
        private Long homeworkId;
        
        private String title;
        
        private String description;
        
        private String subjectName;
        
        private String teacherName;
        
        private LocalDate assignedDate;
        
        private LocalDate dueDate;
        
        private String status; // PENDING, SUBMITTED, OVERDUE, GRADED
        
        private Integer daysUntilDue; // negative if overdue
        
        private String submittedDate;
        
        private String grade;
        
        private String feedback;
    }
}
