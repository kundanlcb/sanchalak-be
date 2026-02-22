package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.StudentRequest;
import com.cm.sanchalak.dto.StudentResponse;
import com.cm.sanchalak.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import com.cm.sanchalak.service.StudentImportService;

@RestController
@RequestMapping("/api/academics/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentImportService studentImportService;

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse student = studentService.createStudent(request);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        StudentResponse student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<StudentResponse>> getAllStudents(
            @RequestParam(required = false) Long classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "rollNo") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(studentService.getAllStudents(classId, page, limit, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping(value = "/bulk-import", consumes = "multipart/form-data")
    public ResponseEntity<String> bulkImportStudents(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }

        try {
            int count = studentImportService.importStudents(file);
            return ResponseEntity.ok("Successfully imported/processed " + count + " students");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import students: " + e.getMessage());
        }
    }
}
