package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification token registration/unregistration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTokenDto {
    
    private String tokenValue;
    private String platform; // FCM, APNS
    private String deviceType; // ANDROID, IOS
    private String deviceId;
    private String appVersion;
    
    /**
     * Response DTO after registration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRegistrationResponse {
        private Long tokenId;
        private String status; // REGISTERED, UPDATED
        private String message;
    }
}
