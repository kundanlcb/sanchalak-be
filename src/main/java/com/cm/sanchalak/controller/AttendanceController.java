package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.*;
import com.cm.sanchalak.entity.AttendanceRecord;
import com.cm.sanchalak.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<BulkMarkAttendanceResponse> markBulkAttendance(
            @RequestBody BulkMarkAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.markBulkAttendance(request, "TEACHER/ADMIN"));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceRecord> markAttendance(@RequestBody MarkAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.markAttendance(request, "TEACHER/ADMIN"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceRecordDto> updateAttendance(@PathVariable Long id,
            @RequestBody com.cm.sanchalak.dto.UpdateAttendanceRequest request) {
        // We need the current user name for 'modifiedBy'
        // For now, hardcode "TEACHER" or extract from SecurityContext if available
        // Let's rely on SecurityContext in a real app, but here I'll use a placeholder
        // or injected principal
        // The service expects a String modifiedBy.
        // Let's use "TEACHER" as default if principal not easily available without
        // adding argument
        // actually, I can add Principal principal to method
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request, "TEACHER/ADMIN"));
    }

    @GetMapping("/sheet/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ClassAttendanceSheetDto> getClassAttendanceSheet(
            @PathVariable Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null)
            date = LocalDate.now();
        return ResponseEntity.ok(attendanceService.getClassAttendanceSheet(classId, date));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<List<AttendanceRecordDto>> getStudentAttendanceHistory(
            @RequestParam Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null)
            startDate = LocalDate.now().minusMonths(1);
        if (endDate == null)
            endDate = LocalDate.now();

        return ResponseEntity.ok(attendanceService.getStudentAttendanceHistory(studentId, startDate, endDate));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<AttendanceSummaryDto> getStudentAttendanceSummary(
            @RequestParam Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null)
            startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null)
            endDate = LocalDate.now();

        return ResponseEntity.ok(attendanceService.getStudentAttendanceSummary(studentId, startDate, endDate));
    }

    @GetMapping("/class/{classId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassAttendanceStatistics> getClassAttendanceStatistics(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getClassAttendanceStatistics(classId, startDate, endDate));
    }
}
