package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.Teacher;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponse {
    private String id;

    @JsonProperty("teacherID")
    private String teacherID;

    private String name;
    private String email;
    private String phone;
    private String mobileNumber;
    private String qualification;
    private String profileImage;
    private List<String> specializedSubjects;
    private String joiningDate;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    public static TeacherResponse from(Teacher teacher) {
        return TeacherResponse.builder()
                .id(String.valueOf(teacher.getId()))
                .teacherID(teacher.getTeacherID())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .phone(teacher.getMobileNumber())
                .mobileNumber(teacher.getMobileNumber())
                .qualification(teacher.getQualification())
                .profileImage(teacher.getProfileImage())
                .specializedSubjects(teacher.getSpecializations() != null ? teacher.getSpecializations().stream()
                        .map(s -> String.valueOf(s.getId()))
                        .collect(Collectors.toList()) : List.of())
                .joiningDate(teacher.getJoiningDate())
                .isActive(!teacher.isDeleted())
                .createdAt(teacher.getCreatedAt() != null ? teacher.getCreatedAt().toString() : null)
                .updatedAt(teacher.getUpdatedAt() != null ? teacher.getUpdatedAt().toString() : null)
                .build();
    }
}
