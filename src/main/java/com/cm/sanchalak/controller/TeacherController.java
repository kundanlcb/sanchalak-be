package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.TeacherRequest;
import com.cm.sanchalak.dto.TeacherResponse;
import com.cm.sanchalak.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academics/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse teacher = teacherService.createTeacher(request);
        return ResponseEntity.ok(teacher);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        TeacherResponse teacher = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(teacher);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }
}
