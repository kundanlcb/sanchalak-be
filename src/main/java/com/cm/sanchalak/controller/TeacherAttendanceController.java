package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.BulkMarkAttendanceResponse;
import com.cm.sanchalak.dto.BulkMarkTeacherAttendanceRequest;
import com.cm.sanchalak.dto.TeacherAttendanceDto;
import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.service.TeacherAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance/teacher")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final TeacherAttendanceService teacherAttendanceService;

    @PostMapping("/mark")
    public ResponseEntity<TeacherAttendanceDto> markIndividualAttendance(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) String remarks) {
        // In a real app, 'markedBy' could be extracted from Principal. Using a default
        // here.
        return ResponseEntity
                .ok(teacherAttendanceService.markAttendance(teacherId, date, status, remarks, "TEACHER/ADMIN"));
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkMarkAttendanceResponse> markBulkAttendance(
            @RequestBody BulkMarkTeacherAttendanceRequest request) {
        return ResponseEntity.ok(teacherAttendanceService.markBulkAttendance(request, "ADMIN"));
    }

    @GetMapping("/sheet")
    public ResponseEntity<List<TeacherAttendanceDto>> getTeacherAttendanceSheet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null)
            date = LocalDate.now();
        return ResponseEntity.ok(teacherAttendanceService.getTeacherAttendanceSheet(date));
    }
}
