package com.cm.sanchalak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * User profile DTO for mobile API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    @JsonProperty("userId")
    private UUID userId;
    private String email;
    private String mobileNumber;
    private String name;
    private String firstName;
    private String lastName;
    private String role;

    // Student-specific fields (populated only for ROLE_STUDENT)
    @JsonProperty("studentId")
    private Long studentId;
    @JsonProperty("studentID")
    private String studentID;

    private String className;
    private Integer rollNo;
    private Integer rollNumber;

    // Parent-specific fields (populated only for ROLE_PARENT)
    @JsonProperty("parentId")
    private Long parentId;
    @JsonProperty("parentID")
    private String parentID;

    // Constructor, Getters, Setters, and Builder are now handled by Lombok @Data,
    // @Builder, @NoArgsConstructor, and @AllArgsConstructor
}
