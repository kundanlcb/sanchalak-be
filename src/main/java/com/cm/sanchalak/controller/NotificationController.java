package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.NotificationTokenDto;
import com.cm.sanchalak.entity.NotificationToken;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for notification token management
 * Unified API for both web and mobile clients
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Register a device token for push notifications
     * Available to all authenticated users (STUDENT, PARENT, TEACHER)
     */
    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<NotificationTokenDto.TokenRegistrationResponse> registerToken(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody NotificationTokenDto tokenDto) {
        
        log.info("Registering notification token for user {}", currentUser.getId());
        
        // Validate input
        if (tokenDto.getTokenValue() == null || tokenDto.getTokenValue().trim().isEmpty()) {
            return ApiResult.error("INVALID_TOKEN", "Token value is required");
        }
        
        if (tokenDto.getPlatform() == null || tokenDto.getPlatform().trim().isEmpty()) {
            return ApiResult.error("INVALID_PLATFORM", "Platform is required (FCM or APNS)");
        }
        
        try {
            NotificationToken token = notificationService.registerToken(
                currentUser.getId(),
                tokenDto.getTokenValue(),
                tokenDto.getPlatform(),
                tokenDto.getDeviceType(),
                tokenDto.getDeviceId(),
                tokenDto.getAppVersion()
            );
            
            NotificationTokenDto.TokenRegistrationResponse response = 
                NotificationTokenDto.TokenRegistrationResponse.builder()
                    .tokenId(token.getId())
                    .status(token.getRegisteredAt().equals(token.getUpdatedAt()) ? "REGISTERED" : "UPDATED")
                    .message("Notification token registered successfully")
                    .build();
            
            return ApiResult.success(response);
            
        } catch (Exception e) {
            log.error("Failed to register notification token for user {}: {}", 
                currentUser.getId(), e.getMessage());
            return ApiResult.error("REGISTRATION_FAILED", "Failed to register notification token: " + e.getMessage());
        }
    }
    
    /**
     * Unregister a device token (logout, app uninstall)
     * Available to all authenticated users
     */
    @PostMapping("/unregister")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<String> unregisterToken(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody NotificationTokenDto tokenDto) {
        
        log.info("Unregistering notification token for user {}", currentUser.getId());
        
        if (tokenDto.getTokenValue() == null || tokenDto.getTokenValue().trim().isEmpty()) {
            return ApiResult.error("INVALID_TOKEN", "Token value is required");
        }
        
        try {
            notificationService.unregisterToken(currentUser.getId(), tokenDto.getTokenValue());
            return ApiResult.success("Notification token unregistered successfully");
            
        } catch (Exception e) {
            log.error("Failed to unregister notification token for user {}: {}", 
                currentUser.getId(), e.getMessage());
            return ApiResult.error("UNREGISTRATION_FAILED", "Failed to unregister notification token: " + e.getMessage());
        }
    }
}
