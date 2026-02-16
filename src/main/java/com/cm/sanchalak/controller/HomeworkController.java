package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.academic.HomeworkRequest;
import com.cm.sanchalak.entity.Homework;
import com.cm.sanchalak.service.HomeworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;

    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Homework> createHomework(@Valid @RequestBody HomeworkRequest request) {
        return ResponseEntity.ok(homeworkService.createHomework(
                request.getClassId(),
                request.getSubjectId(),
                request.getTeacherId(),
                request.getTitle(),
                request.getDescription(),
                request.getDueDate()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<Homework>> getAllHomework() {
        return ResponseEntity.ok(homeworkService.getAllHomework());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Homework> updateHomework(@PathVariable Long id, @Valid @RequestBody HomeworkRequest request) {
        return ResponseEntity.ok(homeworkService.updateHomework(
                id,
                request.getClassId(),
                request.getSubjectId(),
                request.getTeacherId(),
                request.getTitle(),
                request.getDescription(),
                request.getDueDate()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteHomework(@PathVariable Long id) {
        homeworkService.deleteHomework(id);
        return ResponseEntity.noContent().build();
    }
}
