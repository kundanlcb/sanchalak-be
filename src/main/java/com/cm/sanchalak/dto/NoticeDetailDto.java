package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

/**
 * DTO for full notice details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDetailDto {
    private Long id;
    private String title;
    private String content; // Full content
    private String priority;
    private String targetRole;
    private LocalDate publishDate;
    private LocalDate expiryDate;
    private boolean isRead;
    private String attachmentUrl;
    private String createdByName;
    private Instant readAt; // When the user read this notice (null if unread)
}
