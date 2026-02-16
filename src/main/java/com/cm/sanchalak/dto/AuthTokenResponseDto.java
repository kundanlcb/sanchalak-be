package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing JWT access token and refresh token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResponseDto {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn; // seconds

    private UserProfileDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileDto {
        private String userId;
        private String mobileNumber;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        // Aliases for consistent identification
        private Long studentId;
        private String studentID;
        private Integer rollNo;
        private Integer rollNumber;
        private Long parentId;
        private String parentID;
    }
}
