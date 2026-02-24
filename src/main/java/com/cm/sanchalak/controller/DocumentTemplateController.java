package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.DocumentTemplateDto;
import com.cm.sanchalak.service.DocumentTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/school/template")
@RequiredArgsConstructor
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;

    @GetMapping
    public ResponseEntity<DocumentTemplateDto> getTemplate() {
        return ResponseEntity.ok(templateService.getTemplate());
    }

    @PutMapping
    public ResponseEntity<DocumentTemplateDto> saveTemplate(@RequestBody DocumentTemplateDto dto) {
        return ResponseEntity.ok(templateService.saveTemplate(dto));
    }

    /**
     * Returns a presigned URL for logo upload.
     * Frontend uploads directly to Azure Blob using the URL.
     */
    @PostMapping("/logo-url")
    public ResponseEntity<Map<String, String>> getLogoUploadUrl(
            @RequestParam String fileName,
            @RequestParam String contentType) {
        String url = templateService.getLogoUploadUrl(fileName, contentType);
        return ResponseEntity.ok(Map.of("uploadUrl", url));
    }
}
