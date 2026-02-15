package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.platform.importing.ImportJob;
import com.cm.sanchalak.platform.importing.ImportService;
import com.cm.sanchalak.platform.importing.ImportType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/schools/{schoolId}/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<ImportJob> uploadImport(@PathVariable UUID schoolId, @RequestParam("type") ImportType type,
            @RequestParam("file") MultipartFile file) {
        ImportJob job = importService.createImportJob(schoolId, type, file);
        importService.processImport(job.getId(), file); // Async call trigger
        return ResponseEntity.ok(job);
    }

    // Status polling endpoint can be added here
}
