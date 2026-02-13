package com.cm.sanchalak.listener;

import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.ParentStudentLink;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.event.AbsenceRecordedEvent;
import com.cm.sanchalak.event.BusProximityEvent;
import com.cm.sanchalak.event.NoticePublishedEvent;
import com.cm.sanchalak.repository.ParentStudentLinkRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Listener for critical events that trigger push notifications
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    
    private final NotificationService notificationService;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    
    /**
     * Handle absence recorded event
     * Sends notification to all linked parents
     */
    @EventListener
    @Async("notificationExecutor")
    public void handleAbsenceRecorded(AbsenceRecordedEvent event) {
        log.info("Processing absence notification for student {}", event.getStudentId());
        
        // Find all parents linked to this student
        List<ParentStudentLink> links = parentStudentLinkRepository
            .findByStudentIdAndIsActiveTrue(event.getStudentId());
        
        for (ParentStudentLink link : links) {
            Parent parent = link.getParent();
            User parentUser = parent.getUser();
            
            if (parentUser != null) {
                notificationService.sendAbsenceNotification(
                    parentUser.getId(),
                    event.getStudentName(),
                    event.getDate()
                );
            }
        }
        
        log.info("Absence notifications sent to {} parents for student {}", 
            links.size(), event.getStudentId());
    }
    
    /**
     * Handle notice published event
     * Sends notification to all target users based on role/specific user list
     */
    @EventListener
    @Async("notificationExecutor")
    public void handleNoticePublished(NoticePublishedEvent event) {
        log.info("Processing notice notification: {} (priority: {})", event.getTitle(), event.getPriority());
        
        // Only send push for HIGH priority notices
        if (!"HIGH".equals(event.getPriority())) {
            log.debug("Skipping push notification for non-HIGH priority notice");
            return;
        }
        
        List<UUID> targetUserIds = event.getTargetUserIds();
        
        if (targetUserIds != null && !targetUserIds.isEmpty()) {
            notificationService.sendNoticeNotification(
                targetUserIds,
                event.getTitle(),
                event.getNoticeId()
            );
            
            log.info("Notice notification sent to {} users", targetUserIds.size());
        } else {
            log.warn("No target users specified for notice notification");
        }
    }
    
    /**
     * Handle bus proximity event
     * Sends notification when bus is approaching student's stop
     */
    @EventListener
    @Async("notificationExecutor")
    public void handleBusProximity(BusProximityEvent event) {
        log.info("Processing bus proximity alert for student {} (ETA: {} min)", 
            event.getStudentId(), event.getEtaMinutes());
        
        // Only send notification if bus is within 10 minutes (configurable threshold)
        if (event.getEtaMinutes() != null && event.getEtaMinutes() > 10) {
            log.debug("Bus is too far ({}min), skipping notification", event.getEtaMinutes());
            return;
        }
        
        // Find student and their parents
        Optional<Student> studentOpt = studentRepository.findById(event.getStudentId());
        if (studentOpt.isEmpty()) {
            log.warn("Student not found for bus proximity alert: {}", event.getStudentId());
            return;
        }
        
        Student student = studentOpt.get();
        UUID studentUserId = student.getUserId();
        
        // Send to student if they have a user account
        if (studentUserId != null) {
            notificationService.sendBusProximityAlert(
                studentUserId,
                event.getRouteName(),
                event.getStopName(),
                event.getEtaMinutes()
            );
        }
        
        // Send to all linked parents
        List<ParentStudentLink> links = parentStudentLinkRepository
            .findByStudentIdAndIsActiveTrue(event.getStudentId());
        
        for (ParentStudentLink link : links) {
            Parent parent = link.getParent();
            User parentUser = parent.getUser();
            
            if (parentUser != null) {
                notificationService.sendBusProximityAlert(
                    parentUser.getId(),
                    event.getRouteName(),
                    event.getStopName(),
                    event.getEtaMinutes()
                );
            }
        }
        
        log.info("Bus proximity alert sent to student and {} parents", links.size());
    }
}
