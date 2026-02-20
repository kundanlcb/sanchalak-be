package com.cm.sanchalak.platform.onboarding;

import com.cm.sanchalak.platform.school.ContactInfo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolOnboardingRequest {

    // School Details
    private String schoolName;
    private String schoolCode;
    private String board;
    private String registrationNumber;
    private String timezone;

    // Contact Details
    private ContactInfo contactInfo;

    // Admin Details
    private String adminName;
    private String adminEmail;
    private String adminPassword; // Optional, can use default policy
    private String adminMobile;

    // Academic Year Details
    private String academicYearName;
    private LocalDate startDate;
    private LocalDate endDate;

    // Subscription Details
    @NotNull(message = "planId is required")
    private UUID planId;
}
