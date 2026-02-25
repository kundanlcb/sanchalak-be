package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.TeacherRequest;
import com.cm.sanchalak.dto.TeacherResponse;
import com.cm.sanchalak.service.TeacherService;
import com.cm.sanchalak.service.storage.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academics/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse teacher = teacherService.createTeacher(request);
        return ResponseEntity.ok(teacher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id,
            @Valid @RequestBody TeacherRequest request) {
        TeacherResponse teacher = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(teacher);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    /**
     * Returns a presigned upload URL for the teacher photo.
     */
    @PostMapping("/{id}/photo-url")
    public ResponseEntity<java.util.Map<String, String>> getPhotoUploadUrl(
            @PathVariable Long id,
            @RequestParam String fileName,
            @RequestParam String contentType) {
        String objectKey = "teachers/" + id + "/photo/" + fileName;
        String uploadUrl = fileStorageService.generateUploadUrl(objectKey, contentType, 15);
        String publicUrl = fileStorageService.getPublicUrl(objectKey);
        return ResponseEntity.ok(java.util.Map.of("uploadUrl", uploadUrl, "publicUrl", publicUrl));
    }
}
