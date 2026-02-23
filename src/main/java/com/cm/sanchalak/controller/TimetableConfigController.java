package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.academics.TimetableSlotDto;
import com.cm.sanchalak.service.academics.TimetableConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academics/timetable-config")
@RequiredArgsConstructor
public class TimetableConfigController {

    private final TimetableConfigService timetableConfigService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT', 'STAFF')")
    public ResponseEntity<List<TimetableSlotDto>> getTimetableSlots() {
        return ResponseEntity.ok(timetableConfigService.getSchoolTimetableSlots());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TimetableSlotDto>> updateTimetableSlots(
            @RequestBody List<TimetableSlotDto> slots) {
        return ResponseEntity.ok(timetableConfigService.updateSchoolTimetableSlots(slots));
    }
}
