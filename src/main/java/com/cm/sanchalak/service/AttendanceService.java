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
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot mark attendance for future dates");
        }

        SchoolClass clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> students = studentRepository.findByStudentClass_Id(request.getClassId());
        
        List<AttendanceRecord> existingRecords = attendanceRepository.findBySchoolClass_IdAndDate(request.getClassId(), request.getDate());
        Map<Long, AttendanceRecord> existingMap = existingRecords.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity()));
        
        List<BulkMarkAttendanceRequest.StudentAttendanceStatus> inputs = request.getAttendances() != null 
                ? request.getAttendances() 
                : new ArrayList<>();
                
        Map<Long, BulkMarkAttendanceRequest.StudentAttendanceStatus> inputMap = inputs.stream()
                .collect(Collectors.toMap(BulkMarkAttendanceRequest.StudentAttendanceStatus::getStudentId, Function.identity()));

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

            BulkMarkAttendanceRequest.StudentAttendanceStatus input = inputMap.get(student.getId());
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

    public ClassAttendanceSheetDto getClassAttendanceSheet(Long classId, LocalDate date) {
        List<AttendanceRecord> records = attendanceRepository.findBySchoolClass_IdAndDate(classId, date);
        
        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        int absent = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        
        List<AttendanceRecordDto> dtos = records.stream().map(this::mapToDto).collect(Collectors.toList());
        
        return ClassAttendanceSheetDto.builder()
                .classId(classId)
                .date(date)
                .presentCount(present)
                .absentCount(absent)
                .totalCount(records.size())
                .students(dtos)
                .build();
    }
    
    public List<AttendanceRecordDto> getStudentAttendanceHistory(Long studentId, LocalDate startDate, LocalDate endDate) {
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
    
    public AttendanceSummaryDto getStudentAttendanceSummary(Long studentId) {
        List<AttendanceRecord> records = attendanceRepository.findByStudentId(studentId);
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

    public ClassAttendanceStatistics getClassAttendanceStatistics(Long classId, LocalDate startDate, LocalDate endDate) {
         List<AttendanceRecord> records = attendanceRepository.findBySchoolClass_IdAndDateBetween(classId, startDate, endDate);
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
