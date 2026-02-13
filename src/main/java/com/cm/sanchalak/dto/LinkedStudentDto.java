package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a student linked to a parent account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedStudentDto {
    
    private Long studentId;
    
    private String firstName;
    
    private String lastName;
    
    private String fullName;
    
    private String className;
    
    private Integer rollNo;
    
    private String relationshipType;  // FATHER, MOTHER, GUARDIAN
    
    private Boolean isPrimary;
    
    private Boolean isActive;
}
