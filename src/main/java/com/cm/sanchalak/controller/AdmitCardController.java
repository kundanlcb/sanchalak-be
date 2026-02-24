package com.cm.sanchalak.controller;

import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates admit cards for students for a given exam term + class.
 * PDF uses demand-bill pipeline: Thymeleaf -> OpenHTMLtoPDF via ReceiptService.
 */
@RestController
@RequestMapping("/api/documents/admit-card")
@RequiredArgsConstructor
public class AdmitCardController {

    private final StudentRepository studentRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<byte[]> generate(@RequestBody AdmitCardRequest request) {
        UUID schoolId = SchoolContext.getSchoolId();

        // 1. Load exam schedules for the class in this term
        List<ExamSchedule> schedules = examScheduleRepository
                .findByExamTerm_IdAndStudentClass_Id(request.getExamTermId(), request.getClassId());

        // 2. Load students for the class
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> !s.isDeleted()
                        && schoolId.equals(s.getSchoolId())
                        && s.getStudentClass() != null
                        && s.getStudentClass().getId().equals(request.getClassId()))
                .collect(Collectors.toList());

        // 3. Build per-student card data
        List<Map<String, Object>> cards = students.stream().map(student -> {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("studentName", student.getName());
            card.put("fatherName",
                    student.getFatherName() != null ? student.getFatherName() : student.getGuardianName());
            card.put("village", student.getAddressVillage() != null ? student.getAddressVillage() : "");
            card.put("className", student.getStudentClass().getName());
            card.put("rollNo", student.getRollNo() != null ? student.getRollNo().toString() : "");
            card.put("admissionNumber", student.getAdmissionNumber());
            card.put("photoUrl", student.getPhotoUrl() != null ? student.getPhotoUrl() : "");

            // Exam rows
            List<Map<String, String>> examRows = schedules.stream()
                    .filter(s -> s.getExamDate() != null)
                    .sorted(Comparator.comparing(ExamSchedule::getExamDate))
                    .map(s -> {
                        Map<String, String> row = new LinkedHashMap<>();
                        row.put("subject", s.getSubject().getName());
                        row.put("date", s.getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        row.put("day", s.getExamDate().getDayOfWeek().getDisplayName(
                                java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH));
                        row.put("time", s.getStartTime() != null
                                ? s.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
                                : "");
                        row.put("duration", s.getDurationMinutes() != null
                                ? s.getDurationMinutes() + " min"
                                : "");
                        row.put("maxMarks", s.getMaxMarks() != null ? s.getMaxMarks().toString() : "");
                        row.put("shift", s.getShift() != null ? s.getShift() : "");
                        return row;
                    }).collect(Collectors.toList());
            card.put("examRows", examRows);
            return card;
        }).collect(Collectors.toList());

        // 4. Template data
        DocumentTemplate template = documentTemplateRepository.findBySchoolId(schoolId).orElse(null);
        String examTermName = schedules.isEmpty() ? "Examination" : schedules.get(0).getExamTerm().getName();

        Map<String, Object> data = new HashMap<>();
        data.put("cards", cards);
        data.put("template", template);
        data.put("examTermName", examTermName);

        byte[] pdf = receiptService.generatePdf("admit-card", data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"admit-cards-" + examTermName.replace(" ", "-") + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Inner request DTO
    @lombok.Data
    public static class AdmitCardRequest {
        private Long examTermId;
        private Long classId;
    }
}
