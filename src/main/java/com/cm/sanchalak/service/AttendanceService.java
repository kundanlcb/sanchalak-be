package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.repository.spec.AttendanceSpecification;
import com.cm.sanchalak.repository.spec.SchoolClassSpecification;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.exception.AppException;
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
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final OwnershipValidator ownership;

    @Transactional
    public BulkMarkAttendanceResponse markBulkAttendance(BulkMarkAttendanceRequest request, String markedBy) {
        log.info("Bulk marking attendance for class: {} on date: {}", request.getClassId(), request.getDate());

        Long classId = request.getClassId();
        SchoolClass clazz = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> students = studentRepository.findAll(StudentSpecification.activeScoped())
                .stream().filter(s -> s.getStudentClass().getId().equals(classId)).toList();

        List<AttendanceRecord> existingRecords = attendanceRepository
                .findAll(AttendanceSpecification.byClassAndDate(classId, request.getDate()));
        Map<Long, AttendanceRecord> existingMap = existingRecords.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity()));

        List<BulkMarkAttendanceRequest.StudentAttendanceStatus> inputs = request.getAttendances() != null
                ? request.getAttendances()
                : new ArrayList<>();

        Map<Long, BulkMarkAttendanceRequest.StudentAttendanceStatus> inputMap = inputs.stream()
                .collect(Collectors.toMap(BulkMarkAttendanceRequest.StudentAttendanceStatus::getStudentId, a -> a));

        List<AttendanceRecord> toSave = new ArrayList<>();
        int markedCount = 0;
        String user = markedBy != null ? markedBy : (request.getMarkedBy() != null ? request.getMarkedBy() : "SYSTEM");

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

            Long sid = student.getId();
            BulkMarkAttendanceRequest.StudentAttendanceStatus input = inputMap.get(sid);

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
    public AttendanceRecord markAttendance(MarkAttendanceRequest request, String markedBy) {
        log.info("Marking attendance for student: {} in class: {} on date: {}", request.getStudentId(),
                request.getClassId(), request.getDate());

        Long studentId = request.getStudentId();
        Long classId = request.getClassId();

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot mark attendance for future dates");
        }

        AttendanceRecord record = attendanceRepository
                .findOne(AttendanceSpecification.byStudentAndDate(studentId, request.getDate()))
                .orElse(new AttendanceRecord());

        if (record.getId() == null) {
            Student student = studentRepository.findOne(StudentSpecification.activeById(studentId))
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            SchoolClass clazz = student.getStudentClass();
            if (clazz == null) {
                if (classId != null) {
                    clazz = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                            .orElseThrow(() -> new RuntimeException("Class not found"));
                } else {
                    throw new RuntimeException("Student has no class assigned and classId not provided");
                }
            }

            record.setStudent(student);
            record.setSchoolClass(clazz);
            record.setDate(request.getDate());
            record.setMarkedBy(markedBy != null ? markedBy : "SYSTEM");
        } else {
            record.setModified(true);
            record.setModifiedBy(markedBy != null ? markedBy : "SYSTEM");
        }

        record.setStatus(request.getStatus());
        record.setRemarks(request.getRemarks());

        return attendanceRepository.save(record);
    }

    @Transactional(readOnly = true)
    public ClassAttendanceSheetDto getClassAttendanceSheet(Long classId, LocalDate date) {
        log.info("Fetching attendance sheet for class: {} on date: {}", classId, date);

        SchoolClass clazz = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> allStudents = studentRepository.findAll(StudentSpecification.activeScoped())
                .stream().filter(s -> s.getStudentClass().getId().equals(classId)).toList();

        List<AttendanceRecord> existingRecords = attendanceRepository
                .findAll(AttendanceSpecification.byClassAndDate(classId, date));

        Map<Long, AttendanceRecord> recordMap = existingRecords.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity()));

        List<AttendanceRecordDto> dtos = new ArrayList<>();
        int present = 0;
        int absent = 0;

        for (Student student : allStudents) {
            AttendanceRecord record = recordMap.get(student.getId());
            AttendanceRecordDto dto = new AttendanceRecordDto();
            dto.setStudentId(student.getId());
            dto.setStudentName(student.getFirstName() + " " + student.getLastName());
            dto.setRollNumber(student.getRollNo() != null ? String.valueOf(student.getRollNo()) : "");
            dto.setClassId(classId);
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

                if (record.getStatus() == AttendanceStatus.PRESENT)
                    present++;
                else if (record.getStatus() == AttendanceStatus.ABSENT)
                    absent++;
            } else {
                dto.setStatus(AttendanceStatus.PRESENT);
                present++;
            }
            dtos.add(dto);
        }

        return ClassAttendanceSheetDto.builder()
                .classId(classId)
                .classID("CLS-" + classId)
                .className(clazz.getName() != null ? clazz.getName()
                        : "Grade " + clazz.getGrade() + "-" + clazz.getSection())
                .date(date)
                .presentCount(present)
                .absentCount(absent)
                .totalCount(allStudents.size())
                .students(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getStudentAttendanceHistory(Long studentId, LocalDate startDate,
            LocalDate endDate) {
        log.info("Fetching attendance history for student: {} from {} to {}", studentId, startDate, endDate);

        studentRepository.findOne(StudentSpecification.activeById(studentId))
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<AttendanceRecord> records;
        if (startDate != null && endDate != null) {
            records = attendanceRepository.findAll(AttendanceSpecification.activeScoped()
                    .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                    .and((root, query, cb) -> cb.between(root.get("date"), startDate, endDate)));
        } else {
            records = attendanceRepository.findAll(AttendanceSpecification.activeScoped()
                    .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId)));
        }
        return records.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryDto getStudentAttendanceSummary(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching attendance summary for student: {} from {} to {}", studentId, startDate, endDate);

        studentRepository.findOne(StudentSpecification.activeById(studentId))
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<AttendanceRecord> records = attendanceRepository.findAll(AttendanceSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                .and((root, query, cb) -> cb.between(root.get("date"), startDate, endDate)));

        int total = records.size();
        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        double pct = total > 0 ? ((double) present / total) * 100 : 0.0;

        return AttendanceSummaryDto.builder()
                .studentId(studentId)
                .totalDays(total)
                .presentDays(present)
                .percentage(pct)
                .build();
    }

    public ClassAttendanceStatistics getClassAttendanceStatistics(Long classId, LocalDate startDate,
            LocalDate endDate) {
        classRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<AttendanceRecord> records = attendanceRepository.findAll(AttendanceSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("schoolClass").get("id"), classId))
                .and((root, query, cb) -> cb.between(root.get("date"), startDate, endDate)));

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
        AttendanceRecord record = attendanceRepository.findOne(AttendanceSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }

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
        dto.setClassId(r.getSchoolClass().getId());
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
