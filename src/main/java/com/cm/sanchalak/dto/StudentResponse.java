package com.cm.sanchalak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    private Long id;
    private String userId;
    private String createdAt;
    private String updatedAt;
    @JsonProperty("studentID")
    private String studentID; // For frontend compatibility
    private String firstName;
    private String lastName;
    private String name;
    private String dateOfBirth;
    private String gender;
    private String email;
    private String guardianName;
    private String guardianMobile;
    @JsonProperty("classId")
    private Long classId;
    @JsonProperty("classID")
    private String classID;
    private String className;
    private String section;
    private Integer rollNo; // For StudentList compatibility
    private Integer rollNumber; // For StudentDetail compatibility
    private String admissionNumber;
    private String mobileNumber;
    private String status;
    private boolean deleted;

    private ClassResponse studentClass; // For backward compatibility with generated models

    // Nested objects for StudentDetail compatibility
    private AddressResponse address;
    private ParentResponse primaryParent;
    private ParentResponse secondaryParent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassResponse {
        private Long id;
        private String name;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressResponse {
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
    public static class ParentResponse {
        private String name;
        private String relationship;
        private String mobileNumber;
        private String email;
        private String occupation;
    }
}
