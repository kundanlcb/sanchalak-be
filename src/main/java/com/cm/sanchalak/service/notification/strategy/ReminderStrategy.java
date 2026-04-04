package com.cm.sanchalak.service.notification.strategy;

import com.cm.sanchalak.dto.ReminderRequest;

public interface ReminderStrategy {
    boolean supports(String reminderType);

    void sendReminder(ReminderRequest request);
}
