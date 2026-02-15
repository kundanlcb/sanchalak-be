package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.platform.operations.SchoolOperationConfig;
import com.cm.sanchalak.platform.operations.SchoolOperationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/schools/{schoolId}/operations")
public class SchoolOperationController {

    private final SchoolOperationRepository repository;

    public SchoolOperationController(SchoolOperationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<SchoolOperationConfig> getOperationConfig(@PathVariable UUID schoolId) {
        return repository.findBySchoolId(schoolId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SchoolOperationConfig> updateOperationConfig(@PathVariable UUID schoolId,
            @RequestBody SchoolOperationConfig config) {
        config.setSchoolId(schoolId);
        // Basic merge logic or strict replacement
        SchoolOperationConfig existing = repository.findBySchoolId(schoolId).orElse(new SchoolOperationConfig());
        existing.setSchoolId(schoolId);
        existing.setAttendanceEnabled(config.isAttendanceEnabled());
        existing.setNoticesEnabled(config.isNoticesEnabled());
        existing.setRoutineEnabled(config.isRoutineEnabled());
        existing.setSaturdayIsWorking(config.isSaturdayIsWorking());

        return ResponseEntity.ok(repository.save(existing));
    }
}
