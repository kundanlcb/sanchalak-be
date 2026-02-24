package com.cm.sanchalak.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    private Long id; // Optional for update

    private String name;

    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private Integer rollNo;

    private Integer rollNumber;

    private String gender; // MALE, FEMALE, OTHER

    private String guardianName;

    private String guardianMobile;

    private String mobileNumber;

    private String admissionNumber;

    private String admissionDate;

    @NotNull(message = "Class ID is required")
    private Long classId;

    private String section;

    private String academicYear;

    private String dateOfBirth;

    private String bloodGroup;

    private AddressRequest address;

    private ParentRequest primaryParent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressRequest {
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParentRequest {
        private String name;
        private String relationship;
        private String mobileNumber;
        private String email;
        private String occupation;
        private Boolean isPrimaryContact;
    }
}
