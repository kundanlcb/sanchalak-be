package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.NotificationLog;
import com.cm.sanchalak.entity.NotificationToken;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.NotificationLogRepository;
import com.cm.sanchalak.repository.NotificationTokenRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.repository.spec.NotificationSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationTokenRepository tokenRepository;
    private final NotificationLogRepository logRepository;
    private final UserRepository userRepository;
    private final OwnershipValidator ownership;

    @Async("notificationExecutor")
    @Transactional
    public void sendNotificationToUser(UUID userId, String title, String message,
            String notificationType, Map<String, String> data) {
        log.info("Sending notification to user {}: {}", userId, title);

        List<NotificationToken> tokens = tokenRepository.findAll(NotificationSpecification.tokenScoped()
                .and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId))
                .and((root, query, cb) -> cb.equal(root.get("isActive"), true)));

        if (tokens.isEmpty()) {
            log.warn("No active notification tokens found for user {}", userId);
            return;
        }

        for (NotificationToken token : tokens) {
            sendToToken(token, title, message, notificationType, data);
        }
    }

    @Async("notificationExecutor")
    @Transactional
    public void sendNotificationToUsers(List<UUID> userIds, String title, String message,
            String notificationType, Map<String, String> data) {
        log.info("Sending notification to {} users: {}", userIds.size(), title);
        for (UUID userId : userIds) {
            sendNotificationToUser(userId, title, message, notificationType, data);
        }
    }

    private void sendToToken(NotificationToken token, String title, String message,
            String notificationType, Map<String, String> data) {
        User user = token.getUser();

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(message)
                    .build();

            Map<String, String> dataPayload = new HashMap<>();
            if (data != null) {
                dataPayload.putAll(data);
            }
            dataPayload.put("type", notificationType);
            dataPayload.put("timestamp", Instant.now().toString());

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

            String response = FirebaseMessaging.getInstance().send(fcmMessage);

            logNotification(user, title, message, notificationType, token.getTokenValue(),
                    token.getPlatform(), "SENT", null, response, dataPayload);

            token.setLastUsedAt(Instant.now());
            tokenRepository.save(token);

            log.info("Notification sent successfully to user {} on platform {}: {}",
                    user.getId(), token.getPlatform(), response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to user {} on platform {}: {}",
                    user.getId(), token.getPlatform(), e.getMessage());

            logNotification(user, title, message, notificationType, token.getTokenValue(),
                    token.getPlatform(), "FAILED", e.getMessage(), null, data);

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

        if (data != null && !data.isEmpty()) {
            try {
                log.setDataPayload(new ObjectMapper().writeValueAsString(data));
            } catch (Exception e) {
                this.log.warn("Failed to serialize data payload: {}", e.getMessage());
            }
        }

        logRepository.save(log);
    }

    @Transactional
    public NotificationToken registerToken(UUID userId, String tokenValue, String platform,
            String deviceType, String deviceId, String appVersion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationToken existingToken = tokenRepository.findByTokenValue(tokenValue).orElse(null);

        if (existingToken != null) {
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

    @Transactional
    public void unregisterToken(UUID userId, String tokenValue) {
        NotificationToken token = tokenRepository.findByUserIdAndTokenValue(userId, tokenValue)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setIsActive(false);
        tokenRepository.save(token);

        log.info("Unregistered notification token for user {}", userId);
    }

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

    @Transactional(readOnly = true)
    public Page<NotificationLog> getNotificationInbox(UUID userId, Pageable pageable) {
        return logRepository.findAll(NotificationSpecification.logScoped()
                .and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId)), pageable);
    }

    @Transactional
    public void markAsRead(Long notificationId, UUID userId) {
        logRepository.markAsRead(notificationId, userId, Instant.now());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return logRepository.countUnreadByUserId(userId);
    }

    @Async("notificationExecutor")
    public void sendLeaveStatusNotification(UUID userId, String leaveTypeName, String status, String comments) {
        Map<String, String> data = new HashMap<>();
        data.put("action", "VIEW_LEAVES");
        data.put("status", status);

        String message = String.format("Your leave request for %s has been %s.",
                leaveTypeName, status.toLowerCase());

        if (comments != null && !comments.isBlank()) {
            message += " Comments: " + comments;
        }

        sendNotificationToUser(
                userId,
                "Leave Request " + (status.equals("APPROVED") ? "Approved" : "Rejected"),
                message,
                "LEAVE_STATUS",
                data);
    }
}
