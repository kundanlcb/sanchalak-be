package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for aggregated calendar events (exams, holidays, notices)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEventDto {
    private String eventType; // EXAM, HOLIDAY, NOTICE, HOMEWORK_DUE
    private Long eventId; // ID of the source entity (examScheduleId, noticeId, etc.)
    private String title;
    private String description;
    private LocalDate eventDate;
    private String priority; // HIGH, MEDIUM, LOW (for notices)
    private String subjectName; // For exams
    private String className; // For exams
    private String metadata; // JSON string with additional data
}
