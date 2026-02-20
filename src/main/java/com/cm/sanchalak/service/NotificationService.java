package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.NotificationLog;
import com.cm.sanchalak.entity.NotificationToken;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.NotificationLogRepository;
import com.cm.sanchalak.repository.NotificationTokenRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for sending push notifications via Firebase Cloud Messaging
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationTokenRepository tokenRepository;
    private final NotificationLogRepository logRepository;
    private final UserRepository userRepository;

    /**
     * Send push notification to a specific user (all their active devices)
     * Runs asynchronously to avoid blocking
     */
    @Async("notificationExecutor")
    @Transactional
    public void sendNotificationToUser(UUID userId, String title, String message,
            String notificationType, Map<String, String> data) {
        log.info("Sending notification to user {}: {}", userId, title);

        List<NotificationToken> tokens = tokenRepository.findActiveByUserId(userId);

        if (tokens.isEmpty()) {
            log.warn("No active notification tokens found for user {}", userId);
            return;
        }

        for (NotificationToken token : tokens) {
            sendToToken(token, title, message, notificationType, data);
        }
    }

    /**
     * Send push notification to multiple users
     */
    @Async("notificationExecutor")
    @Transactional
    public void sendNotificationToUsers(List<UUID> userIds, String title, String message,
            String notificationType, Map<String, String> data) {
        log.info("Sending notification to {} users: {}", userIds.size(), title);

        for (UUID userId : userIds) {
            sendNotificationToUser(userId, title, message, notificationType, data);
        }
    }

    /**
     * Send notification to a specific token
     */
    private void sendToToken(NotificationToken token, String title, String message,
            String notificationType, Map<String, String> data) {
        User user = token.getUser();

        try {
            // Build notification payload
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(message)
                    .build();

            // Build data payload
            Map<String, String> dataPayload = new HashMap<>();
            if (data != null) {
                dataPayload.putAll(data);
            }
            dataPayload.put("type", notificationType);
            dataPayload.put("timestamp", Instant.now().toString());

            // Build message
            Message fcmMessage = Message.builder()
                    .setToken(token.getTokenValue())
                    .setNotification(notification)
                    .putAllData(dataPayload)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(fcmMessage);

            // Log success
            logNotification(user, title, message, notificationType, token.getTokenValue(),
                    token.getPlatform(), "SENT", null, response, dataPayload);

            // Update last used timestamp
            token.setLastUsedAt(Instant.now());
            tokenRepository.save(token);

            log.info("Notification sent successfully to user {} on platform {}: {}",
                    user.getId(), token.getPlatform(), response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to user {} on platform {}: {}",
                    user.getId(), token.getPlatform(), e.getMessage());

            // Log failure
            logNotification(user, title, message, notificationType, token.getTokenValue(),
                    token.getPlatform(), "FAILED", e.getMessage(), null, data);

            // Handle invalid token
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                    e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("Token is invalid, marking as inactive: {}", token.getId());
                token.setIsActive(false);
                tokenRepository.save(token);
            }
        } catch (Exception e) {
            log.error("Unexpected error sending notification to user {}: {}", user.getId(), e.getMessage());
            logNotification(user, title, message, notificationType, token.getTokenValue(),
                    token.getPlatform(), "FAILED", e.getMessage(), null, data);
        }
    }

    /**
     * Log notification to database
     */
    private void logNotification(User user, String title, String message, String notificationType,
            String targetToken, String platform, String status, String errorMessage,
            String referenceId, Map<String, String> data) {
        NotificationLog log = new NotificationLog();
        log.setUser(user);
        log.setNotificationType(notificationType);
        log.setTitle(title);
        log.setBody(message);
        log.setTargetToken(targetToken);
        log.setPlatform(platform);
        log.setDeliveryStatus(status);
        log.setFailureReason(errorMessage);
        log.setMessageId(referenceId);
        log.setSentAt(Instant.now());

        if ("SENT".equals(status)) {
            log.setDeliveredAt(Instant.now());
        }

        // Store data payload as JSON string
        if (data != null && !data.isEmpty()) {
            try {
                log.setDataPayload(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data));
            } catch (Exception e) {
                this.log.warn("Failed to serialize data payload: {}", e.getMessage());
            }
        }

        logRepository.save(log);
    }

    /**
     * Register a new notification token
     */
    @Transactional
    public NotificationToken registerToken(UUID userId, String tokenValue, String platform,
            String deviceType, String deviceId, String appVersion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if token already exists
        NotificationToken existingToken = tokenRepository.findByTokenValue(tokenValue).orElse(null);

        if (existingToken != null) {
            // Update existing token
            existingToken.setUser(user);
            existingToken.setPlatform(platform);
            existingToken.setDeviceType(deviceType);
            existingToken.setDeviceId(deviceId);
            existingToken.setAppVersion(appVersion);
            existingToken.setIsActive(true);
            existingToken.setLastUsedAt(Instant.now());

            log.info("Updated existing notification token for user {}", userId);
            return tokenRepository.save(existingToken);
        }

        // Create new token
        NotificationToken token = new NotificationToken();
        token.setUser(user);
        token.setTokenValue(tokenValue);
        token.setPlatform(platform);
        token.setDeviceType(deviceType);
        token.setDeviceId(deviceId);
        token.setAppVersion(appVersion);
        token.setIsActive(true);
        token.setRegisteredAt(Instant.now());
        token.setLastUsedAt(Instant.now());

        log.info("Registered new notification token for user {}", userId);
        return tokenRepository.save(token);
    }

    /**
     * Unregister a notification token
     */
    @Transactional
    public void unregisterToken(UUID userId, String tokenValue) {
        NotificationToken token = tokenRepository.findByUserIdAndTokenValue(userId, tokenValue)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setIsActive(false);
        tokenRepository.save(token);

        log.info("Unregistered notification token for user {}", userId);
    }

    /**
     * Send absence notification to parent
     */
    @Async("notificationExecutor")
    public void sendAbsenceNotification(UUID parentUserId, String studentName, String date) {
        Map<String, String> data = new HashMap<>();
        data.put("studentName", studentName);
        data.put("date", date);
        data.put("action", "VIEW_ATTENDANCE");

        sendNotificationToUser(
                parentUserId,
                "Absence Alert",
                String.format("%s was marked absent on %s", studentName, date),
                "ABSENCE",
                data);
    }

    /**
     * Send fee due reminder to parent
     */
    @Async("notificationExecutor")
    public void sendFeeDueReminder(UUID parentUserId, String studentName, Double amount, String dueDate) {
        Map<String, String> data = new HashMap<>();
        data.put("studentName", studentName);
        data.put("amount", amount.toString());
        data.put("dueDate", dueDate);
        data.put("action", "VIEW_FEES");

        sendNotificationToUser(
                parentUserId,
                "Fee Payment Reminder",
                String.format("Fee payment of ₹%.2f for %s is due on %s", amount, studentName, dueDate),
                "FEE_DUE",
                data);
    }

    /**
     * Send notice notification
     */
    @Async("notificationExecutor")
    public void sendNoticeNotification(List<UUID> userIds, String noticeTitle, Long noticeId) {
        Map<String, String> data = new HashMap<>();
        data.put("noticeId", noticeId.toString());
        data.put("action", "VIEW_NOTICE");

        sendNotificationToUsers(
                userIds,
                "New Notice",
                noticeTitle,
                "NOTICE",
                data);
    }

    /**
     * Send bus proximity alert
     */
    @Async("notificationExecutor")
    public void sendBusProximityAlert(UUID userId, String routeName, String stopName, Integer etaMinutes) {
        Map<String, String> data = new HashMap<>();
        data.put("routeName", routeName);
        data.put("stopName", stopName);
        data.put("etaMinutes", etaMinutes.toString());
        data.put("action", "VIEW_BUS_TRACKING");

        sendNotificationToUser(
                userId,
                "Bus Approaching",
                String.format("Bus %s will arrive at %s in %d minutes", routeName, stopName, etaMinutes),
                "BUS_ALERT",
                data);
    }

    /**
     * Get user's notification inbox
     */
    @Transactional(readOnly = true)
    public Page<NotificationLog> getNotificationInbox(UUID userId, Pageable pageable) {
        return logRepository.findByUserIdOrderBySentAtDesc(userId, pageable);
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, UUID userId) {
        logRepository.markAsRead(notificationId, userId, Instant.now());
    }

    /**
     * Get count of unread notifications
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return logRepository.countUnreadByUserId(userId);
    }
}
