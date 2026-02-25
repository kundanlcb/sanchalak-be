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

@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    // Classes
    @PostMapping("/classes")
    public ResponseEntity<SchoolClass> createClass(@Valid @RequestBody SchoolClass schoolClass) {
        return ResponseEntity.ok(academicService.createClass(schoolClass));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<SchoolClass>> getAllClasses() {
        return ResponseEntity.ok(academicService.getAllClasses());
    }

    // Terms
    @PostMapping("/terms")
    public ResponseEntity<ExamTerm> createTerm(@Valid @RequestBody ExamTermRequest request) {
        ExamTerm term = new ExamTerm();
        term.setName(request.getName());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        return ResponseEntity.ok(academicService.createExamTerm(term));
    }

    @GetMapping("/terms")
    public ResponseEntity<List<ExamTerm>> getAllTerms() {
        return ResponseEntity.ok(academicService.getAllTerms());
    }

    @PutMapping("/terms/{id}")
    public ResponseEntity<ExamTerm> updateTerm(
            @PathVariable Long id,
            @Valid @RequestBody ExamTermRequest request) {
        ExamTerm termDetails = new ExamTerm();
        termDetails.setName(request.getName());
        termDetails.setStartDate(request.getStartDate());
        termDetails.setEndDate(request.getEndDate());
        return ResponseEntity.ok(academicService.updateExamTerm(id, termDetails));
    }

    @DeleteMapping("/terms/{id}")
    public ResponseEntity<Void> deleteTerm(@PathVariable Long id) {
        academicService.deleteExamTerm(id);
        return ResponseEntity.noContent().build();
    }

    // Subjects
    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody SubjectRequest request) {
        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setClassId(request.getClassId());
        return ResponseEntity.ok(academicService.createSubject(subject));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(academicService.getAllSubjects());
    }

    // Class Subjects
    @PostMapping("/class-subjects")
    public ResponseEntity<ClassSubject> assignSubjectToClass(@Valid @RequestBody ClassSubjectRequest request) {
        return ResponseEntity.ok(academicService.assignSubjectToClass(
                request.getClassId(),
                request.getSubjectId(),
                request.getTeacherId()));
    }

    // Schedules
    @PostMapping("/schedules")
    public ResponseEntity<ExamSchedule> scheduleExam(@Valid @RequestBody ExamScheduleRequest request) {
        return ResponseEntity.ok(academicService.scheduleExam(
                request.getExamTermId(),
                request.getClassId(),
                request.getSubjectId(),
                request.getExamDate(),
                request.getMaxMarks(),
                request.getPassingMarks(),
                request.getStartTime(),
                request.getEndTime(),
                request.getDurationMinutes()));
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<ExamSchedule> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ExamScheduleRequest request) {
        return ResponseEntity.ok(academicService.updateSchedule(id,
                request.getExamDate(),
                request.getMaxMarks(),
                request.getPassingMarks(),
                request.getStartTime(),
                request.getEndTime(),
                request.getDurationMinutes()));
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        academicService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<ExamSchedule>> getSchedules(
            @RequestParam(required = false) Long examTermId,
            @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(academicService.getSchedules(examTermId, classId));
    }

    // Marks
    @PostMapping("/marks")
    public ResponseEntity<StudentMarks> saveStudentMarks(@Valid @RequestBody MarkEntryRequest request) {
        return ResponseEntity.ok(academicService.saveStudentMarks(
                request.getExamScheduleId(),
                request.getStudentId(),
                request.getMarksObtained(),
                request.getRemarks()));
    }

    @PostMapping("/marks/bulk")
    public ResponseEntity<List<StudentMarks>> saveBulkStudentMarks(
            @Valid @RequestBody com.cm.sanchalak.dto.academic.BulkMarkEntryRequest request) {
        return ResponseEntity.ok(academicService.saveBulkStudentMarks(
                request.getExamTermId(),
                request.getClassId(),
                request.getSubjectId(),
                request.getMarks()));
    }

    @GetMapping("/marks")
    public ResponseEntity<List<StudentMarks>> getMarks(
            @RequestParam(required = false) Long examTermId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(academicService.getMarks(examTermId, classId, subjectId, studentId));
    }

    // Reports
    @GetMapping("/reports/{studentId}")
    public ResponseEntity<ReportCardDto> getReportCard(@PathVariable Long studentId) {
        return ResponseEntity.ok(academicService.generateReportCard(studentId));
    }

    @GetMapping("/marks/class/{classId}")
    public ResponseEntity<List<ReportCardDto>> getMarksForClass(
            @PathVariable Long classId,
            @RequestParam Long termId) {
        return ResponseEntity.ok(academicService.getClassTermMarks(classId, termId));
    }

    // Class Management

    @PutMapping("/classes/{id}")
    public ResponseEntity<SchoolClass> updateClass(@PathVariable Long id, @Valid @RequestBody SchoolClass schoolClass) {
        return ResponseEntity.ok(academicService.updateClass(id, schoolClass));
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        academicService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    // Subject Management

    @PutMapping("/subjects/{id}")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(academicService.updateSubject(id, request.getName(), request.getCode()));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        academicService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    // --- Exam Question Paper ---

    @PostMapping("/schedules/{scheduleId}/questions")
    public ResponseEntity<com.cm.sanchalak.dto.academic.ExamQuestionDto> addQuestionToExam(
            @PathVariable Long scheduleId,
            @Valid @RequestBody com.cm.sanchalak.dto.academic.ExamQuestionRequest request) {
        return ResponseEntity.ok(academicService.addQuestionToExam(scheduleId, request));
    }

    @GetMapping("/schedules/{scheduleId}/questions")
    public ResponseEntity<List<com.cm.sanchalak.dto.academic.ExamQuestionDto>> getExamQuestions(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(academicService.getExamQuestions(scheduleId));
    }

    @PutMapping("/schedules/{scheduleId}/questions")
    public ResponseEntity<List<com.cm.sanchalak.dto.academic.ExamQuestionDto>> setExamQuestions(
            @PathVariable Long scheduleId,
            @Valid @RequestBody List<com.cm.sanchalak.dto.academic.ExamQuestionRequest> requests) {
        return ResponseEntity.ok(academicService.setExamQuestions(scheduleId, requests));
    }

    @DeleteMapping("/schedules/{scheduleId}/questions/{examQuestionId}")
    public ResponseEntity<Void> removeQuestionFromExam(
            @PathVariable Long scheduleId,
            @PathVariable Long examQuestionId) {
        academicService.removeQuestionFromExam(scheduleId, examQuestionId);
        return ResponseEntity.noContent().build();
    }
}
