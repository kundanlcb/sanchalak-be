package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.BulkMarkTeacherAttendanceRequest;
import com.cm.sanchalak.dto.BulkMarkAttendanceResponse;
import com.cm.sanchalak.dto.TeacherAttendanceDto;
import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.entity.TeacherAttendance;
import com.cm.sanchalak.repository.TeacherAttendanceRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.security.SchoolContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAttendanceService {

    private final TeacherAttendanceRepository teacherAttendanceRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public TeacherAttendanceDto markAttendance(Long teacherId, LocalDate date, AttendanceStatus status, String remarks,
            String markedBy) {
        log.info("Marking attendance for teacher: {} on date: {}", teacherId, date);

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot mark attendance for future dates");
        }

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        UUID schoolId = SchoolContext.getSchoolId();
        if (!teacher.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to teacher resource");
        }

        TeacherAttendance record = teacherAttendanceRepository.findByTeacherIdAndDate(teacherId, date)
                .orElse(new TeacherAttendance());

        if (record.getId() == null) {
            record.setTeacher(teacher);
            record.setDate(date);
            record.setSchoolId(schoolId);
            record.setMarkedBy(markedBy != null ? markedBy : "SYSTEM");
        } else {
            record.setModified(true);
            record.setModifiedBy(markedBy != null ? markedBy : "SYSTEM");
        }

        record.setStatus(status);
        record.setRemarks(remarks);

        record = teacherAttendanceRepository.save(record);
        return mapToDto(record);
    }

    @Transactional
    public BulkMarkAttendanceResponse markBulkAttendance(BulkMarkTeacherAttendanceRequest request, String markedBy) {
        log.info("Bulk marking teacher attendance on date: {}", request.getDate());

        UUID schoolId = SchoolContext.getSchoolId();
        List<Teacher> teachers = teacherRepository.findBySchoolIdAndDeletedFalse(schoolId);

        List<TeacherAttendance> existingRecords = teacherAttendanceRepository.findBySchoolIdAndDate(schoolId,
                request.getDate());
        Map<Long, TeacherAttendance> existingMap = existingRecords.stream()
                .collect(Collectors.toMap(r -> r.getTeacher().getId(), Function.identity()));

        List<BulkMarkTeacherAttendanceRequest.TeacherAttendanceStatus> inputs = request.getAttendances() != null
                ? request.getAttendances()
                : new ArrayList<>();
        Map<Long, BulkMarkTeacherAttendanceRequest.TeacherAttendanceStatus> inputMap = inputs.stream()
                .collect(Collectors.toMap(BulkMarkTeacherAttendanceRequest.TeacherAttendanceStatus::getTeacherId,
                        a -> a));

        List<TeacherAttendance> toSave = new ArrayList<>();
        int markedCount = 0;
        String user = markedBy != null ? markedBy : (request.getMarkedBy() != null ? request.getMarkedBy() : "SYSTEM");

        for (Teacher teacher : teachers) {
            TeacherAttendance record = existingMap.getOrDefault(teacher.getId(), new TeacherAttendance());

            if (record.getId() == null) {
                record.setTeacher(teacher);
                record.setDate(request.getDate());
                record.setSchoolId(schoolId);
                record.setMarkedBy(user);
            } else {
                record.setModifiedBy(user);
                record.setModified(true);
            }

            BulkMarkTeacherAttendanceRequest.TeacherAttendanceStatus input = inputMap.get(teacher.getId());

            if (input != null) {
                record.setStatus(input.getStatus() != null ? input.getStatus() : AttendanceStatus.PRESENT);
                record.setRemarks(input.getRemarks());
            } else {
                record.setStatus(AttendanceStatus.PRESENT);
            }

            toSave.add(record);
            markedCount++;
        }

        teacherAttendanceRepository.saveAll(toSave);
        return new BulkMarkAttendanceResponse(true, markedCount, 0, "Teacher attendance marked successfully");
    }

    @Transactional(readOnly = true)
    public List<TeacherAttendanceDto> getTeacherAttendanceSheet(LocalDate date) {
        UUID schoolId = SchoolContext.getSchoolId();

        List<Teacher> teachers = teacherRepository.findBySchoolIdAndDeletedFalse(schoolId);
        List<TeacherAttendance> existingRecords = teacherAttendanceRepository.findBySchoolIdAndDate(schoolId, date);
        Map<Long, TeacherAttendance> recordMap = existingRecords.stream()
                .collect(Collectors.toMap(r -> r.getTeacher().getId(), Function.identity()));

        List<TeacherAttendanceDto> dtos = new ArrayList<>();

        for (Teacher teacher : teachers) {
            TeacherAttendance record = recordMap.get(teacher.getId());
            TeacherAttendanceDto dto = new TeacherAttendanceDto();
            dto.setTeacherId(teacher.getId());
            dto.setTeacherName(teacher.getName());
            dto.setTeacherEmail(teacher.getEmail());
            dto.setDate(date);

            if (record != null) {
                dto.setId(record.getId());
                dto.setStatus(record.getStatus());
                dto.setRemarks(record.getRemarks());
                dto.setMarkedBy(record.getMarkedBy());
                if (record.getCreatedAt() != null) {
                    dto.setMarkedDate(LocalDateTime.ofInstant(record.getCreatedAt(), ZoneId.systemDefault()));
                }
                dto.setModified(record.isModified());
            } else {
                dto.setStatus(null); // Not marked yet
            }
            dtos.add(dto);
        }

        return dtos;
    }

    private TeacherAttendanceDto mapToDto(TeacherAttendance r) {
        TeacherAttendanceDto dto = new TeacherAttendanceDto();
        dto.setId(r.getId());
        dto.setTeacherId(r.getTeacher().getId());
        dto.setTeacherName(r.getTeacher().getName());
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
