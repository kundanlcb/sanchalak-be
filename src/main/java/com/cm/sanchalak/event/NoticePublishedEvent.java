package com.cm.sanchalak.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Event published when a high-priority notice is created
 */
@Data
@AllArgsConstructor
public class NoticePublishedEvent {
    private Long noticeId;
    private String title;
    private String priority; // HIGH, MEDIUM, LOW
    private String targetRole; // PARENT, STUDENT, TEACHER, ALL
    private List<UUID> targetUserIds; // Specific users to notify
}
