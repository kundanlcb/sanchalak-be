package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.Teacher;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponse {
    private Long id;
    @JsonProperty("teacherID")
    private String teacherID;
    private String name;
    private String email;
    private String phone;
    private String mobileNumber;
    private String qualification;
    private String profileImage;
    private Set<Subject> specializations;
    private String createdAt;
    private String updatedAt;

    public static TeacherResponse from(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .teacherID(String.valueOf(teacher.getId()))
                .name(teacher.getName())
                .email(teacher.getEmail())
                .phone(teacher.getMobileNumber())
                .mobileNumber(teacher.getMobileNumber())
                .qualification(teacher.getQualification())
                .profileImage(teacher.getProfileImage())
                .specializations(teacher.getSpecializations())
                .createdAt(teacher.getCreatedAt() != null ? teacher.getCreatedAt().toString() : null)
                .updatedAt(teacher.getUpdatedAt() != null ? teacher.getUpdatedAt().toString() : null)
                .build();
    }
}
