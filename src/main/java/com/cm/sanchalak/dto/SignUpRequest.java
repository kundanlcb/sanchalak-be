package com.cm.sanchalak.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @NotBlank
    @Size(min = 4, max = 40)
    String name,

    @NotBlank
    @Size(max = 40)
    @Email
    String email,

    @NotBlank
    @Size(min = 6, max = 20)
    String password,

    String role
) {}
