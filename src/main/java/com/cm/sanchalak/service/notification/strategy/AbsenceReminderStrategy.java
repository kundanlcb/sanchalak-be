package com.cm.sanchalak.service.notification.strategy;

import com.cm.sanchalak.dto.ReminderRequest;
import com.cm.sanchalak.dto.StudentResponse;
import com.cm.sanchalak.service.NotificationService;
import com.cm.sanchalak.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AbsenceReminderStrategy implements ReminderStrategy {

    private final StudentService studentService;
    private final NotificationService notificationService;

    @Override
    public boolean supports(String reminderType) {
        return "ABSENCE".equalsIgnoreCase(reminderType);
    }

    @Override
    public void sendReminder(ReminderRequest request) {
        log.info("Processing ABSENCE reminder for student {}", request.getStudentId());

        StudentResponse student = studentService.getStudentById(request.getStudentId());
        if (student.getUserId() == null) {
            throw new RuntimeException("This student has no linked user account for notifications");
        }

        UUID userId = UUID.fromString(student.getUserId());
        String studentName = student.getName() != null ? student.getName()
                : ((student.getFirstName() != null ? student.getFirstName() : "") + " " +
                        (student.getLastName() != null ? student.getLastName() : "")).trim();

        // Extract metadata if provided, else use today
        String date = request.getMetadata() != null && request.getMetadata().containsKey("date")
                ? String.valueOf(request.getMetadata().get("date"))
                : LocalDate.now().toString();

        notificationService.sendAbsenceNotification(userId, studentName, date);
    }
}
