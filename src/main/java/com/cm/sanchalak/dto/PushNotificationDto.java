package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for push notification payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationDto {
    
    private String title;
    private String message;
    private String notificationType; // ABSENCE, FEE_DUE, NOTICE, BUS_ALERT, HOMEWORK_DUE
    private Map<String, String> data; // Additional payload data
    
    /**
     * Response DTO after sending notification
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private String status; // SENT, FAILED, PARTIAL
        private Integer successCount;
        private Integer failureCount;
        private String message;
    }
}
