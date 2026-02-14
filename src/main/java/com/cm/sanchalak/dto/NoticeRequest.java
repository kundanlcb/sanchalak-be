package com.cm.sanchalak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NoticeRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Priority is required")
    private String priority; // HIGH, MEDIUM, LOW

    @NotNull(message = "Target role is required")
    private String targetRole; // PARENT, STUDENT, TEACHER, ALL

    private LocalDate publishDate;
    private LocalDate expiryDate;
    private String attachmentUrl;
}
