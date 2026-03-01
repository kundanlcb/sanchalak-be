package com.cm.sanchalak.controller;

import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.service.AttendanceService;
import com.cm.sanchalak.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates marksheets / report cards for students for a given exam term +
 * class.
 */
@RestController
@RequestMapping("/api/documents/marksheet")
@RequiredArgsConstructor
public class MarksheetController {

    private final StudentRepository studentRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final StudentMarksRepository studentMarksRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final AttendanceService attendanceService;
    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<byte[]> generateSingle(@RequestBody MarksheetRequest request) {
        UUID schoolId = SchoolContext.getSchoolId();

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!schoolId.equals(student.getSchoolId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<ExamSchedule> schedules = examScheduleRepository
                .findByExamTerm_IdAndStudentClass_Id(request.getExamTermId(), student.getStudentClass().getId());

        List<Student> students = Collections.singletonList(student);
        List<Student> allStudentsInClass = studentRepository.findAll().stream()
                .filter(s -> !s.isDeleted() && schoolId.equals(s.getSchoolId()) && s.getStudentClass() != null
                        && s.getStudentClass().getId().equals(student.getStudentClass().getId()))
                .collect(Collectors.toList());

        return buildPdfResponse(students, allStudentsInClass, schedules, schoolId, request);
    }

    @PostMapping("/bulk")
    public ResponseEntity<byte[]> generateBulk(@RequestBody MarksheetRequest request) {
        UUID schoolId = SchoolContext.getSchoolId();

        List<ExamSchedule> schedules = examScheduleRepository
                .findByExamTerm_IdAndStudentClass_Id(request.getExamTermId(), request.getClassId());

        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> !s.isDeleted()
                        && schoolId.equals(s.getSchoolId())
                        && s.getStudentClass() != null
                        && s.getStudentClass().getId().equals(request.getClassId()))
                .sorted(Comparator.comparingInt(s -> s.getRollNo() != null ? s.getRollNo() : 999))
                .collect(Collectors.toList());

        return buildPdfResponse(students, students, schedules, schoolId, request);
    }

    private ResponseEntity<byte[]> buildPdfResponse(List<Student> targetStudents, List<Student> allStudents,
            List<ExamSchedule> schedules, UUID schoolId, MarksheetRequest request) {
        String examTermName = schedules.isEmpty() ? "Examination" : schedules.get(0).getExamTerm().getName();

        // 1. Compute subject-level marks per student
        List<Map<String, Object>> marksheets = targetStudents.stream().map(student -> {
            // Subject rows
            List<Map<String, Object>> subjectRows = schedules.stream().map(schedule -> {
                Optional<StudentMarks> marksOpt = studentMarksRepository
                        .findByExamScheduleAndStudent(schedule, student);

                double obtained = marksOpt.map(StudentMarks::getMarksObtained).orElse(0.0);
                int max = schedule.getMaxMarks() != null ? schedule.getMaxMarks() : 100;
                int passing = schedule.getPassingMarks() != null ? schedule.getPassingMarks() : 33;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("subject", schedule.getSubject().getName());
                row.put("maxMarks", max);
                row.put("obtained", marksOpt.isPresent() ? obtained : "AB");
                row.put("passing", passing);
                row.put("status", marksOpt.isPresent() && obtained >= passing ? "P" : "F");
                return row;
            }).collect(Collectors.toList());

            // Totals
            double totalObtained = subjectRows.stream()
                    .filter(r -> r.get("obtained") instanceof Double)
                    .mapToDouble(r -> (Double) r.get("obtained")).sum();
            int totalMax = subjectRows.stream()
                    .mapToInt(r -> (Integer) r.get("maxMarks")).sum();
            double percentage = totalMax > 0 ? (totalObtained / totalMax) * 100 : 0;
            String grade = computeGrade(percentage);
            boolean isPass = subjectRows.stream().noneMatch(r -> "F".equals(r.get("status")));

            // Rank among class
            int rank = computeRank(student, schedules, allStudents);

            // Attendance — use current academic year dates as approximation
            int presentDays = 0;
            int totalDays = 0;
            try {
                var summary = attendanceService.getStudentAttendanceSummary(
                        student.getId(), request.getFromDate(), request.getToDate());
                presentDays = summary.getPresentDays();
                totalDays = summary.getTotalDays();
            } catch (Exception ignored) {
                // Attendance may not be available for all students
            }

            Map<String, Object> sheet = new LinkedHashMap<>();
            sheet.put("studentName", student.getName());
            sheet.put("fatherName",
                    student.getFatherName() != null ? student.getFatherName() : student.getGuardianName());
            sheet.put("motherName", student.getMotherName() != null ? student.getMotherName() : "");
            sheet.put("className", student.getStudentClass().getName());
            sheet.put("rollNo", student.getRollNo() != null ? student.getRollNo().toString() : "");
            sheet.put("admissionNumber", student.getAdmissionNumber());
            sheet.put("subjectRows", subjectRows);
            sheet.put("totalObtained", totalObtained);
            sheet.put("totalMax", totalMax);
            sheet.put("percentage", String.format("%.1f", percentage));
            sheet.put("grade", grade);
            sheet.put("result", isPass ? "PASS" : "FAIL");
            sheet.put("rank", rank);
            sheet.put("presentDays", presentDays);
            sheet.put("totalDays", totalDays);
            return sheet;
        }).collect(Collectors.toList());

        DocumentTemplate template = documentTemplateRepository.findBySchoolId(schoolId).orElse(null);

        Map<String, Object> data = new HashMap<>();
        data.put("marksheets", marksheets);
        data.put("template", template);
        data.put("examTermName", examTermName);

        byte[] pdf = receiptService.generatePdf("marksheet", data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"marksheets-" + examTermName.replace(" ", "-") + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String computeGrade(double pct) {
        if (pct >= 91)
            return "A1";
        if (pct >= 81)
            return "A2";
        if (pct >= 71)
            return "B1";
        if (pct >= 61)
            return "B2";
        if (pct >= 51)
            return "C1";
        if (pct >= 41)
            return "C2";
        if (pct >= 33)
            return "D";
        return "E";
    }

    private int computeRank(Student target, List<ExamSchedule> schedules, List<Student> allStudents) {
        Map<Long, Double> totals = allStudents.stream().collect(Collectors.toMap(
                Student::getId,
                s -> schedules.stream().mapToDouble(sch -> {
                    Optional<StudentMarks> m = studentMarksRepository.findByExamScheduleAndStudent(sch, s);
                    return m.map(StudentMarks::getMarksObtained).orElse(0.0);
                }).sum()));
        double myTotal = totals.getOrDefault(target.getId(), 0.0);
        return (int) totals.values().stream().filter(v -> v > myTotal).count() + 1;
    }

    // Inner request DTO
    @lombok.Data
    public static class MarksheetRequest {
        private Long examTermId;
        private Long classId;
        private Long studentId;
        private LocalDate fromDate;
        private LocalDate toDate;
    }
}
