package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.CreateDocumentRequest;
import com.cm.sanchalak.dto.PresignedUrlDto;
import com.cm.sanchalak.dto.StudentDocumentDto;
import com.cm.sanchalak.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/upload-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResult<PresignedUrlDto>> getUploadUrl(
            @RequestParam String fileName,
            @RequestParam String mimeType) {
        return ResponseEntity.ok(ApiResult.success(documentService.generateUploadUrl(fileName, mimeType)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResult<StudentDocumentDto>> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return ResponseEntity.ok(ApiResult.success(documentService.createDocument(request)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<ApiResult<List<StudentDocumentDto>>> getDocuments(@PathVariable Long studentId) {
        // TODO: specific authorization for PARENT/STUDENT to check linkage
        return ResponseEntity.ok(ApiResult.success(documentService.getDocuments(studentId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<Void>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResult.success(null));
    }
}
