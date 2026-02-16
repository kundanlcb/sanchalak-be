package com.cm.sanchalak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentRequest {

    private Long id; // Optional for update

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Roll number is required")
    private Integer rollNo;

    private Integer rollNumber; // Alias for rollNo

    @NotBlank(message = "Gender is required")
    private String gender; // MALE, FEMALE, OTHER

    private String guardianName;

    private String guardianMobile;

    private String mobileNumber; // Alias for guardianMobile

    @NotNull(message = "Class ID is required")
    private Long classId;
}
