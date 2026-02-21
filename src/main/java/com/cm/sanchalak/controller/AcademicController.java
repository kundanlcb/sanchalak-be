package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.academic.ClassSubjectRequest;
import com.cm.sanchalak.dto.academic.ExamTermRequest;
import com.cm.sanchalak.dto.academic.SubjectRequest;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.ClassSubject;
import com.cm.sanchalak.entity.ExamTerm;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.ExamSchedule;
import com.cm.sanchalak.dto.academic.ExamScheduleRequest;
import com.cm.sanchalak.dto.academic.MarkEntryRequest;
import com.cm.sanchalak.dto.academic.ReportCardDto;
import com.cm.sanchalak.entity.StudentMarks;
import com.cm.sanchalak.service.AcademicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    // Classes
    @PostMapping("/classes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolClass> createClass(@Valid @RequestBody SchoolClass schoolClass) {
        return ResponseEntity.ok(academicService.createClass(schoolClass));
    }

    @GetMapping("/classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<SchoolClass>> getAllClasses() {
        return ResponseEntity.ok(academicService.getAllClasses());
    }

    // Terms
    @PostMapping("/terms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamTerm> createTerm(@Valid @RequestBody ExamTermRequest request) {
        ExamTerm term = new ExamTerm();
        term.setName(request.getName());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        return ResponseEntity.ok(academicService.createExamTerm(term));
    }

    @GetMapping("/terms")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ExamTerm>> getAllTerms() {
        return ResponseEntity.ok(academicService.getAllTerms());
    }

    @PutMapping("/terms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamTerm> updateTerm(
            @PathVariable Long id,
            @Valid @RequestBody ExamTermRequest request) {
        ExamTerm termDetails = new ExamTerm();
        termDetails.setName(request.getName());
        termDetails.setStartDate(request.getStartDate());
        termDetails.setEndDate(request.getEndDate());
        return ResponseEntity.ok(academicService.updateExamTerm(id, termDetails));
    }

    // Subjects
    @PostMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody SubjectRequest request) {
        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        return ResponseEntity.ok(academicService.createSubject(subject));
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(academicService.getAllSubjects());
    }

    // Class Subjects
    @PostMapping("/class-subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassSubject> assignSubjectToClass(@Valid @RequestBody ClassSubjectRequest request) {
        return ResponseEntity.ok(academicService.assignSubjectToClass(
                request.getClassId(),
                request.getSubjectId(),
                request.getTeacherId()));
    }

    // Schedules
    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ExamSchedule> scheduleExam(@Valid @RequestBody ExamScheduleRequest request) {
        return ResponseEntity.ok(academicService.scheduleExam(
                request.getExamTermId(),
                request.getClassId(),
                request.getSubjectId(),
                request.getExamDate(),
                request.getMaxMarks()));
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ExamSchedule>> getSchedules(
            @RequestParam(required = false) Long examTermId,
            @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(academicService.getSchedules(examTermId, classId));
    }

    // Marks
    @PostMapping("/marks")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<StudentMarks> saveStudentMarks(@Valid @RequestBody MarkEntryRequest request) {
        return ResponseEntity.ok(academicService.saveStudentMarks(
                request.getExamScheduleId(),
                request.getStudentId(),
                request.getMarksObtained(),
                request.getRemarks()));
    }

    @PostMapping("/marks/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<StudentMarks>> saveBulkStudentMarks(
            @Valid @RequestBody com.cm.sanchalak.dto.academic.BulkMarkEntryRequest request) {
        return ResponseEntity.ok(academicService.saveBulkStudentMarks(
                request.getExamTermId(),
                request.getClassId(),
                request.getSubjectId(),
                request.getMarks()));
    }

    @GetMapping("/marks")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<StudentMarks>> getMarks(
            @RequestParam(required = false) Long examTermId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(academicService.getMarks(examTermId, classId, subjectId, studentId));
    }

    // Reports
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @GetMapping("/reports/{studentId}")
    public ResponseEntity<ReportCardDto> getReportCard(@PathVariable Long studentId) {
        return ResponseEntity.ok(academicService.generateReportCard(studentId));
    }

    @GetMapping("/marks/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReportCardDto>> getMarksForClass(
            @PathVariable Long classId,
            @RequestParam Long termId) {
        return ResponseEntity.ok(academicService.getClassTermMarks(classId, termId));
    }

    // Class Management

    @PutMapping("/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolClass> updateClass(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(academicService.updateClass(id, name));
    }

    @DeleteMapping("/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        academicService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    // Subject Management

    @PutMapping("/subjects/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(academicService.updateSubject(id, request.getName(), request.getCode()));
    }

    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        academicService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}

// Class Management
