package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for homework submission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkSubmissionDto {
    
    private Long submissionId;
    
    private Long homeworkId;
    
    private String homeworkTitle;
    
    private Long studentId;
    
    private String studentName;
    
    private Instant submittedAt;
    
    private Boolean isLate;
    
    private String status; // SUBMITTED, RESUBMITTED, GRADED, RETURNED
    
    private List<String> fileUrls;
    
    private String studentRemarks;
    
    private String teacherFeedback;
    
    private String grade;
    
    private Double marksObtained;
    
    private Instant gradedAt;
    
    private String gradedByName;
}
