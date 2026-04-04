package com.cm.sanchalak.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderRequest {
    private Long studentId;
    private String reminderType; // e.g. "FEE_DUE", "ABSENCE", "HOMEWORK_DUE"
    private Map<String, Object> metadata;
}
