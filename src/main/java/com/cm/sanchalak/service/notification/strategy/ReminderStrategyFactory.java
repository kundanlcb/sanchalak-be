package com.cm.sanchalak.service.notification.strategy;

import com.cm.sanchalak.dto.ReminderRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderStrategyFactory {

    private final List<ReminderStrategy> strategies;

    public ReminderStrategyFactory(List<ReminderStrategy> strategies) {
        this.strategies = strategies;
    }

    public void processReminder(ReminderRequest request) {
        if (request == null || request.getReminderType() == null) {
            throw new IllegalArgumentException("Reminder type is required");
        }

        ReminderStrategy strategy = strategies.stream()
                .filter(s -> s.supports(request.getReminderType()))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unsupported reminder type: " + request.getReminderType()));

        strategy.sendReminder(request);
    }
}
