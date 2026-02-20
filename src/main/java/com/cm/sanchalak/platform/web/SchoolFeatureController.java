package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.platform.school.SchoolFeatureEntitlementService;
import com.cm.sanchalak.platform.school.dto.SchoolFeatureStateDto;
import com.cm.sanchalak.platform.school.dto.SchoolFeatureToggleRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/schools/{schoolId}/features")
public class SchoolFeatureController {

    private final SchoolFeatureEntitlementService schoolFeatureEntitlementService;

    public SchoolFeatureController(SchoolFeatureEntitlementService schoolFeatureEntitlementService) {
        this.schoolFeatureEntitlementService = schoolFeatureEntitlementService;
    }

    @GetMapping
    public ResponseEntity<List<SchoolFeatureStateDto>> getSchoolFeatures(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(schoolFeatureEntitlementService.getSchoolFeatureStates(schoolId));
    }

    @PutMapping("/{featureId}")
    public ResponseEntity<SchoolFeatureStateDto> toggleSchoolFeature(
            @PathVariable UUID schoolId,
            @PathVariable UUID featureId,
            @RequestBody SchoolFeatureToggleRequest request) {
        if (request.getEnabled() == null) {
            throw new IllegalArgumentException("enabled is required");
        }

        return ResponseEntity.ok(schoolFeatureEntitlementService.setFeatureEnabled(
                schoolId,
                featureId,
                request.getEnabled()));
    }
}
