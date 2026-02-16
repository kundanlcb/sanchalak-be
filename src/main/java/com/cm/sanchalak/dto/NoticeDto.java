package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for notice listing with read status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {
    private Long id;
    private String title;
    private String priority; // HIGH, MEDIUM, LOW
    private String targetRole; // PARENT, STUDENT, TEACHER, ALL
    private LocalDate publishDate;
    private LocalDate expiryDate;
    private boolean isRead; // Whether the current user has read this notice
    private String attachmentUrl;
    private String createdByName; // Name of user who created the notice
}
