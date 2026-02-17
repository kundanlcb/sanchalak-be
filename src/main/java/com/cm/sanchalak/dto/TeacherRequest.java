package com.cm.sanchalak.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    private String mobileNumber;

    private String qualification;
    private String profileImage;
    private String joiningDate;

    private List<Long> specializationIds;

    public String getMobileNumber() {
        return mobileNumber != null ? mobileNumber : phone;
    }
}
