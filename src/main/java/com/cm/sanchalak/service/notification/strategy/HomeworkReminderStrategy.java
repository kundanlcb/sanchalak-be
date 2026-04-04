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
public class HomeworkReminderStrategy implements ReminderStrategy {

    private final StudentService studentService;
    private final NotificationService notificationService;

    @Override
    public boolean supports(String reminderType) {
        return "HOMEWORK_DUE".equalsIgnoreCase(reminderType);
    }

    @Override
    public void sendReminder(ReminderRequest request) {
        log.info("Processing HOMEWORK_DUE reminder for student {}", request.getStudentId());

        StudentResponse student = studentService.getStudentById(request.getStudentId());
        if (student.getUserId() == null) {
            throw new RuntimeException("This student has no linked user account for notifications");
        }

        UUID userId = UUID.fromString(student.getUserId());
        String studentName = student.getName() != null ? student.getName()
                : ((student.getFirstName() != null ? student.getFirstName() : "") + " " +
                        (student.getLastName() != null ? student.getLastName() : "")).trim();

        String subject = request.getMetadata() != null && request.getMetadata().containsKey("subject")
                ? String.valueOf(request.getMetadata().get("subject"))
                : "a subject";

        String dueDate = request.getMetadata() != null && request.getMetadata().containsKey("dueDate")
                ? String.valueOf(request.getMetadata().get("dueDate"))
                : LocalDate.now().toString();

        notificationService.sendHomeworkDueReminder(userId, studentName, subject, dueDate);
    }
}
