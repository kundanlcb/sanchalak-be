package com.cm.sanchalak.controller.academics;

import com.cm.sanchalak.dto.academics.HolidayDto;
import com.cm.sanchalak.service.academics.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academics/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<HolidayDto>> getAllHolidays(
            @RequestParam(defaultValue = "2024-2025") String academicYear) {
        return ResponseEntity.ok(holidayService.getAllHolidays(academicYear));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<HolidayDto> createHoliday(@RequestBody HolidayDto holidayDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayService.createHoliday(holidayDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<HolidayDto> updateHoliday(@PathVariable Long id, @RequestBody HolidayDto holidayDto) {
        return ResponseEntity.ok(holidayService.updateHoliday(id, holidayDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/seed-national")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> seedNationalHolidays(@RequestParam String academicYear) {
        holidayService.seedNationalHolidays(academicYear);
        return ResponseEntity.ok("National holidays seeded successfully.");
    }
}
