package com.cm.sanchalak.security.dynamic;

import com.cm.sanchalak.platform.school.School;
import com.cm.sanchalak.platform.school.SchoolRepository;
import com.cm.sanchalak.entity.TimetableSlot;
import com.cm.sanchalak.repository.TimetableSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimetableSlotSeeder {

    private final TimetableSlotRepository timetableSlotRepository;
    private final SchoolRepository schoolRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Order(110)
    @Transactional
    public void seedDefaultTimetables() {
        log.info("Checking timetable slots for existing schools...");

        List<School> schools = schoolRepository.findAll();
        int seededCount = 0;

        for (School school : schools) {
            List<TimetableSlot> existingSlots = timetableSlotRepository
                    .findBySchoolIdOrderByOrderIndexAsc(school.getId());
            if (existingSlots.isEmpty()) {
                seedDefaultSlotsForSchool(school);
                seededCount++;
            }
        }

        if (seededCount > 0) {
            log.info("Successfully seeded default timetable slots for {} schools.", seededCount);
        }
    }

    private void seedDefaultSlotsForSchool(School school) {
        List<TimetableSlot> slots = new ArrayList<>();

        // 8 AM to 2 PM, 40 minute periods
        LocalTime time = LocalTime.of(8, 0);

        for (int i = 1; i <= 8; i++) {
            if (i == 4) {
                // Break after period 3
                slots.add(TimetableSlot.builder()
                        .schoolId(school.getId())
                        .name("Break")
                        .isBreak(true)
                        .orderIndex(i)
                        .startTime(time)
                        .endTime(time.plusMinutes(20))
                        .build());
                time = time.plusMinutes(20);
            }

            String periodName = "Period " + (i >= 4 ? i - 1 : i);
            if (i >= 4) {
                periodName = "Period " + i; // Matching the legacy Period 4 naming index
            }

            slots.add(TimetableSlot.builder()
                    .schoolId(school.getId())
                    .name(periodName)
                    .isBreak(false)
                    .orderIndex(i > 3 ? i + 1 : i) // adjust order index to account for break
                    .startTime(time)
                    .endTime(time.plusMinutes(40))
                    .build());

            time = time.plusMinutes(40);
        }

        timetableSlotRepository.saveAll(slots);
    }
}
