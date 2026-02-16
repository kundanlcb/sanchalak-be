package com.cm.sanchalak.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponse {
    private Long id;
    private String name;
    private String firstName;
    private String lastName;
    private Integer rollNo;
    private String gender;
    private String guardianName;
    private String guardianMobile;
    private Long classId; // Keep for backward compat if needed
    private String classID; // For frontend compatibility
    private String className;
    private String section;
    private String admissionNumber;
    private String mobileNumber;
    private String status;
    private boolean deleted;
}
