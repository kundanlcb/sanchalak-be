package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.NotificationTokenDto;
import com.cm.sanchalak.dto.StudentResponse;
import com.cm.sanchalak.entity.NotificationToken;
import com.cm.sanchalak.entity.NotificationLog;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.NotificationService;
import com.cm.sanchalak.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final StudentService studentService;

    /**
     * Register a device token for push notifications
     * Available to all authenticated users (STUDENT, PARENT, TEACHER)
     */
    @PostMapping("/register")
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
                    tokenDto.getAppVersion());

            NotificationTokenDto.TokenRegistrationResponse response = NotificationTokenDto.TokenRegistrationResponse
                    .builder()
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
            return ApiResult.error("UNREGISTRATION_FAILED",
                    "Failed to unregister notification token: " + e.getMessage());
        }
    }

    /**
     * Get notification inbox
     */
    @GetMapping("/inbox")
    public ApiResult<Page<NotificationLog>> getInbox(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Fetching notification inbox for user {}", currentUser.getId());
        try {
            return ApiResult.success(notificationService.getNotificationInbox(currentUser.getId(), pageable));
        } catch (Exception e) {
            log.error("Failed to fetch notification inbox for user {}: {}", currentUser.getId(), e.getMessage());
            return ApiResult.error("INBOX_FETCH_FAILED", "Failed to fetch notification inbox");
        }
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread-count")
    public ApiResult<Long> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        log.info("Fetching unread notification count for user {}", currentUser.getId());
        try {
            return ApiResult.success(notificationService.getUnreadCount(currentUser.getId()));
        } catch (Exception e) {
            log.error("Failed to fetch unread count for user {}: {}", currentUser.getId(), e.getMessage());
            return ApiResult.error("COUNT_FETCH_FAILED", "Failed to fetch unread count");
        }
    }

    /**
     * Mark notification as read
     */
    @PostMapping("/{id}/read")
    public ApiResult<String> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {

        log.info("Marking notification {} as read for user {}", id, currentUser.getId());
        try {
            notificationService.markAsRead(id, currentUser.getId());
            return ApiResult.success("Marked as read");
        } catch (Exception e) {
            log.error("Failed to mark notification {} as read: {}", id, e.getMessage());
            return ApiResult.error("MARK_READ_FAILED", "Failed to mark as read");
        }
    }

    /**
     * Send a fee payment reminder notification to the student / parent.
     * Accessible only by Admin.
     */
    @PostMapping("/payment-reminder/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<String> sendPaymentReminder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long studentId) {

        log.info("Admin {} sending payment reminder for student {}", currentUser.getId(), studentId);
        try {
            StudentResponse student = studentService.getStudentById(studentId);

            if (student.getUserId() == null) {
                return ApiResult.error("NO_USER", "This student has no linked user account for notifications");
            }

            java.util.UUID userId = java.util.UUID.fromString(student.getUserId());
            String studentName = student.getName() != null ? student.getName()
                    : ((student.getFirstName() != null ? student.getFirstName() : "") + " " +
                            (student.getLastName() != null ? student.getLastName() : "")).trim();

            // Use a sensible default amount / due date; the client can enhance this later
            notificationService.sendFeeDueReminder(userId, studentName, 0.0, "as soon as possible");

            return ApiResult.success("Payment reminder sent to student " + studentName);
        } catch (Exception e) {
            log.error("Failed to send payment reminder for student {}: {}", studentId, e.getMessage());
            return ApiResult.error("REMINDER_FAILED", "Failed to send payment reminder: " + e.getMessage());
        }
    }
}
