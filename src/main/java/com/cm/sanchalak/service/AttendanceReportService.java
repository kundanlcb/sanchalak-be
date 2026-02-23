package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.AttendanceRecord;
import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.entity.TeacherAttendance;
import com.cm.sanchalak.repository.AttendanceRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.TeacherAttendanceRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.spec.AttendanceSpecification;
import com.cm.sanchalak.security.SchoolContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceReportService {

    private final AttendanceRepository attendanceRepository;
    private final TeacherAttendanceRepository teacherAttendanceRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public DailyAttendanceReport getDailyDetailedReport(LocalDate date) {
        UUID schoolId = SchoolContext.getSchoolId();

        // 1. Process Students
        List<Student> students = studentRepository.findBySchoolIdAndDeletedFalse(schoolId);
        List<AttendanceRecord> studentRecords = attendanceRepository.findAll(
                AttendanceSpecification.activeScoped()
                        .and((root, query, cb) -> cb.equal(root.get("date"), date)));
        Map<Long, AttendanceRecord> studentRecordMap = studentRecords.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity()));

        List<StudentDailyReportDto> studentDtos = new ArrayList<>();
        int studentPresent = 0;
        int studentAbsent = 0;

        for (Student s : students) {
            AttendanceRecord r = studentRecordMap.get(s.getId());
            StudentDailyReportDto dto = new StudentDailyReportDto();
            dto.setId(s.getId());
            dto.setName(s.getFirstName() + " " + s.getLastName());
            dto.setClassName(s.getStudentClass() != null ? s.getStudentClass().getName() : "Unassigned");

            if (r != null) {
                dto.setStatus(r.getStatus());
                dto.setRemarks(r.getRemarks());
                if (r.getStatus() == AttendanceStatus.PRESENT)
                    studentPresent++;
                else if (r.getStatus() == AttendanceStatus.ABSENT)
                    studentAbsent++;
            } else {
                dto.setStatus(null); // Not marked
            }
            studentDtos.add(dto);
        }

        // 2. Process Teachers
        List<Teacher> teachers = teacherRepository.findBySchoolIdAndDeletedFalse(schoolId);
        List<TeacherAttendance> teacherRecords = teacherAttendanceRepository.findBySchoolIdAndDate(schoolId, date);
        Map<Long, TeacherAttendance> teacherRecordMap = teacherRecords.stream()
                .collect(Collectors.toMap(r -> r.getTeacher().getId(), Function.identity()));

        List<TeacherDailyReportDto> teacherDtos = new ArrayList<>();
        int teacherPresent = 0;
        int teacherAbsent = 0;

        for (Teacher t : teachers) {
            TeacherAttendance r = teacherRecordMap.get(t.getId());
            TeacherDailyReportDto dto = new TeacherDailyReportDto();
            dto.setId(t.getId());
            dto.setName(t.getName());

            if (r != null) {
                dto.setStatus(r.getStatus());
                dto.setRemarks(r.getRemarks());
                if (r.getStatus() == AttendanceStatus.PRESENT)
                    teacherPresent++;
                else if (r.getStatus() == AttendanceStatus.ABSENT)
                    teacherAbsent++;
            } else {
                dto.setStatus(null); // Not marked
            }
            teacherDtos.add(dto);
        }

        return DailyAttendanceReport.builder()
                .date(date)
                .studentStats(new Stats(students.size(), studentPresent, studentAbsent))
                .teacherStats(new Stats(teachers.size(), teacherPresent, teacherAbsent))
                .students(studentDtos)
                .teachers(teacherDtos)
                .build();
    }

    @Data
    @Builder
    public static class DailyAttendanceReport {
        private LocalDate date;
        private Stats studentStats;
        private Stats teacherStats;
        private List<StudentDailyReportDto> students;
        private List<TeacherDailyReportDto> teachers;
    }

    @Data
    @AllArgsConstructor
    public static class Stats {
        private int total;
        private int present;
        private int absent;
    }

    @Data
    public static class StudentDailyReportDto {
        private Long id;
        private String name;
        private String className;
        private AttendanceStatus status;
        private String remarks;
    }

    @Data
    public static class TeacherDailyReportDto {
        private Long id;
        private String name;
        private AttendanceStatus status;
        private String remarks;
    }
}
