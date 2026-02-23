package com.cm.sanchalak.security.dynamic;

import com.cm.sanchalak.service.academics.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(3) // After role seeder
public class HolidaySeederListener implements ApplicationListener<ApplicationReadyEvent> {

    private final HolidayService holidayService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("ApplicationReadyEvent received. Seeding National Holidays...");
        // For now, hardcode "2024-2025" or get from global settings.
        try {
            holidayService.seedNationalHolidays("2024-2025");
        } catch (Exception e) {
            log.error("Failed to seed national holidays", e);
        }
    }
}
