package com.cm.sanchalak.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer rollNo;
    private String gender;
    private String guardianName;
    private String guardianMobile;
    private Long classId;
    private String className; // Helpful for UI
    private boolean deleted;
}
