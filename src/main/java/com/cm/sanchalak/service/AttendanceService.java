package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;

    @Transactional
    public BulkMarkAttendanceResponse markBulkAttendance(BulkMarkAttendanceRequest request) {
        Long classId = resolveClassId(request.getClassId());
        SchoolClass clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> students = studentRepository.findByStudentClass_Id(classId);

        List<AttendanceRecord> existingRecords = attendanceRepository.findBySchoolClass_IdAndDate(classId,
                request.getDate());
        Map<Long, AttendanceRecord> existingMap = existingRecords.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity()));

        List<BulkMarkAttendanceRequest.StudentAttendanceStatus> inputs = request.getAttendances() != null
                ? request.getAttendances()
                : new ArrayList<>();

        Map<String, BulkMarkAttendanceRequest.StudentAttendanceStatus> inputMap = inputs.stream()
                .collect(Collectors.toMap(BulkMarkAttendanceRequest.StudentAttendanceStatus::getStudentId,
                        Function.identity()));

        List<AttendanceRecord> toSave = new ArrayList<>();
        int markedCount = 0;
        String user = request.getMarkedBy() != null ? request.getMarkedBy() : "SYSTEM";

        for (Student student : students) {
            AttendanceRecord record = existingMap.getOrDefault(student.getId(), new AttendanceRecord());

            if (record.getId() == null) {
                record.setStudent(student);
                record.setSchoolClass(clazz);
                record.setDate(request.getDate());
                record.setMarkedBy(user);
            } else {
                record.setModifiedBy(user);
                record.setModified(true);
            }

            // Derive human-readable studentID for lookup (e.g., STU-1)
            String humanId = "STU-" + student.getId();
            // Handle case where input might have full human ID or just numeric ID as String
            BulkMarkAttendanceRequest.StudentAttendanceStatus input = inputMap.get(humanId);
            if (input == null) {
                input = inputMap.get(student.getId().toString());
            }

            if (input != null) {
                record.setStatus(input.getStatus() != null ? input.getStatus() : AttendanceStatus.PRESENT);
                record.setRemarks(input.getRemarks());
            } else {
                record.setStatus(AttendanceStatus.PRESENT);
            }

            toSave.add(record);
            markedCount++;
        }

        attendanceRepository.saveAll(toSave);

        return new BulkMarkAttendanceResponse(true, markedCount, 0, "Attendance marked successfully");
    }

    @Transactional
    public AttendanceRecord markAttendance(MarkAttendanceRequest request) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot mark attendance for future dates");
        }

        AttendanceRecord record = attendanceRepository.findByStudentIdAndDate(request.getStudentId(), request.getDate())
                .orElse(new AttendanceRecord());

        if (record.getId() == null) {
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            SchoolClass clazz = student.getStudentClass();
            if (clazz == null) {
                if (request.getClassId() != null) {
                    clazz = classRepository.findById(request.getClassId())
                            .orElseThrow(() -> new RuntimeException("Class not found"));
                } else {
                    throw new RuntimeException("Student has no class assigned and classId not provided");
                }
            }

            record.setStudent(student);
            record.setSchoolClass(clazz);
            record.setDate(request.getDate());
            record.setMarkedBy("SYSTEM");
        } else {
            record.setModified(true);
            record.setModifiedBy("SYSTEM");
        }

        record.setStatus(request.getStatus());
        record.setRemarks(request.getRemarks());

        return attendanceRepository.save(record);
    }

    public ClassAttendanceSheetDto getClassAttendanceSheet(String classIdStr, LocalDate date) {
        Long classId = resolveClassId(classIdStr);
        List<AttendanceRecord> records = attendanceRepository.findBySchoolClass_IdAndDate(classId, date);

        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        int absent = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();

        List<AttendanceRecordDto> dtos = records.stream().map(this::mapToDto).collect(Collectors.toList());

        return ClassAttendanceSheetDto.builder()
                .classId(classId)
                .classID(classIdStr.startsWith("CLS-") ? classIdStr : "CLS-01-" + classId)
                .date(date)
                .presentCount(present)
                .absentCount(absent)
                .totalCount(records.size())
                .students(dtos)
                .build();
    }

    private Long resolveClassId(String classIdStr) {
        if (classIdStr == null || classIdStr.isEmpty()) {
            throw new IllegalArgumentException("Class ID cannot be null or empty");
        }

        try {
            return Long.parseLong(classIdStr);
        } catch (NumberFormatException e) {
            // Not a plain number
        }

        if (classIdStr.startsWith("CLS-")) {
            // Try to see if it's "CLS-numeric" or "CLS-XX-numeric"
            String[] parts = classIdStr.split("-");
            String lastPart = parts[parts.length - 1];
            try {
                return Long.parseLong(lastPart);
            } catch (NumberFormatException e) {
                // Not numeric
            }
        }

        // Search by name as last resort?
        return classRepository.findByName(classIdStr)
                .map(SchoolClass::getId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID/Name: " + classIdStr));
    }

    public List<AttendanceRecordDto> getStudentAttendanceHistory(String studentIdStr, LocalDate startDate,
            LocalDate endDate) {
        Long studentId = resolveStudentId(studentIdStr);
        List<AttendanceRecord> records;
        if (startDate != null && endDate != null) {
            records = attendanceRepository.findByStudentIdAndDateBetween(studentId, startDate, endDate);
        } else {
            records = attendanceRepository.findByStudentId(studentId);
        }
        return records.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AttendanceSummaryDto getStudentAttendanceSummary(String studentIdStr) {
        Long studentId = resolveStudentId(studentIdStr);
        List<AttendanceRecord> records = attendanceRepository.findByStudentId(studentId);
        int total = records.size();
        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        double pct = total > 0 ? ((double) present / total) * 100 : 0.0;

        return AttendanceSummaryDto.builder()
                .studentId(studentId)
                .studentID(studentIdStr.startsWith("STU-") ? studentIdStr : "STU-" + studentId)
                .totalDays(total)
                .presentDays(present)
                .percentage(pct)
                .build();
    }

    private Long resolveStudentId(String studentIdStr) {
        if (studentIdStr == null || studentIdStr.isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }

        // 1. Try if it's a plain numeric ID
        try {
            return Long.parseLong(studentIdStr);
        } catch (NumberFormatException e) {
            // Not a plain number
        }

        // 2. Try if it's "STU-X" format
        if (studentIdStr.startsWith("STU-")) {
            // Try to see if it's "STU-numeric"
            String numericPart = studentIdStr.substring(4);
            try {
                return Long.parseLong(numericPart);
            } catch (NumberFormatException e) {
                // Might be STU-2026-00001 format
            }

            // 3. Search by studentID field in database
            return studentRepository.findByStudentID(studentIdStr)
                    .map(Student::getId)
                    .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentIdStr));
        }

        // 4. Try as UUID (userId)
        try {
            java.util.UUID userId = java.util.UUID.fromString(studentIdStr);
            return studentRepository.findByUserId(userId)
                    .map(Student::getId)
                    .orElseThrow(() -> new RuntimeException("Student not found for User ID: " + studentIdStr));
        } catch (IllegalArgumentException e) {
            // Not a UUID
        }

        throw new RuntimeException("Invalid Student ID format: " + studentIdStr);
    }

    public ClassAttendanceStatistics getClassAttendanceStatistics(Long classId, LocalDate startDate,
            LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRepository.findBySchoolClass_IdAndDateBetween(classId, startDate,
                endDate);
        int total = records.size();
        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        double pct = total > 0 ? ((double) present / total) * 100 : 0.0;

        long days = records.stream().map(AttendanceRecord::getDate).distinct().count();

        return ClassAttendanceStatistics.builder()
                .classId(classId)
                .averagePercentage(pct)
                .totalWorkingDays((int) days)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Transactional
    public AttendanceRecordDto updateAttendance(Long id, UpdateAttendanceRequest request, String modifiedBy) {
        AttendanceRecord record = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        // Validation: Cannot update past a certain window?
        // For now, allow update if it's not in the future (which isn't possible for
        // creation anyway)

        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }

        // Update remarks if provided (allow empty string to clear?)
        // Let's assume null means no change, empty string acts as clear if intended,
        // but usually for PUT we replace.
        // But for partial update (PATCH behavior), we check null.
        // Requirement says Correction, implies PUT usually.
        if (request.getRemarks() != null) {
            record.setRemarks(request.getRemarks());
        }

        record.setModified(true);
        record.setModifiedBy(modifiedBy);

        record = attendanceRepository.save(record);

        return mapToDto(record);
    }

    private AttendanceRecordDto mapToDto(AttendanceRecord r) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(r.getId());
        dto.setStudentId(r.getStudent().getId());
        dto.setStudentID("STU-" + r.getStudent().getId());
        dto.setClassId(r.getSchoolClass().getId());
        dto.setClassID("CLS-01-" + r.getSchoolClass().getId());
        dto.setDate(r.getDate());
        dto.setStatus(r.getStatus());
        dto.setRemarks(r.getRemarks());
        dto.setMarkedBy(r.getMarkedBy());
        if (r.getCreatedAt() != null) {
            dto.setMarkedDate(LocalDateTime.ofInstant(r.getCreatedAt(), ZoneId.systemDefault()));
        }
        dto.setModified(r.isModified());
        return dto;
    }
}
