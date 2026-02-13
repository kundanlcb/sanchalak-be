package com.cm.sanchalak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for scheduled tasks (fee reminders, bus proximity monitoring)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Scheduling configuration
    // Jobs are defined in scheduler package with @Scheduled annotations
}
