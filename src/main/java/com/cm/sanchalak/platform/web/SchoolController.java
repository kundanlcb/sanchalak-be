package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.platform.onboarding.BootstrapAdminRequest;
import com.cm.sanchalak.platform.onboarding.BootstrapAdminService;
import com.cm.sanchalak.platform.onboarding.OnboardingStatus;
import com.cm.sanchalak.platform.school.School;
import com.cm.sanchalak.platform.school.SchoolService;
import com.cm.sanchalak.platform.school.SchoolStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/schools")
public class SchoolController {

    private final SchoolService schoolService;
    private final BootstrapAdminService bootstrapAdminService;

    public SchoolController(SchoolService schoolService, BootstrapAdminService bootstrapAdminService) {
        this.schoolService = schoolService;
        this.bootstrapAdminService = bootstrapAdminService;
    }

    @GetMapping
    public ResponseEntity<List<School>> getAllSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @PostMapping
    public ResponseEntity<School> createSchool(@RequestBody School school) {
        return ResponseEntity.ok(schoolService.createSchool(school));
    }

    @GetMapping("/{schoolId}")
    public ResponseEntity<School> getSchoolById(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(schoolService.getSchoolById(schoolId));
    }

    @PostMapping("/{schoolId}/status-transition")
    public ResponseEntity<School> transitionStatus(@PathVariable UUID schoolId, @RequestBody SchoolStatus status) {
        return ResponseEntity.ok(schoolService.transitionStatus(schoolId, status));
    }

    @PostMapping("/{schoolId}/bootstrap-admin")
    public ResponseEntity<User> bootstrapAdmin(@PathVariable UUID schoolId,
            @RequestBody BootstrapAdminRequest request) {
        return ResponseEntity.ok(bootstrapAdminService.bootstrapAdmin(schoolId, request));
    }

    @PutMapping("/{schoolId}")
    public ResponseEntity<School> updateSchool(@PathVariable UUID schoolId, @RequestBody School school) {
        return ResponseEntity.ok(schoolService.updateSchool(schoolId, school));
    }

    @GetMapping("/{schoolId}/onboarding-status")
    public ResponseEntity<OnboardingStatus> getOnboardingStatus(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(schoolService.getOnboardingStatus(schoolId));
    }

    @PostMapping("/onboard")
    public ResponseEntity<School> onboardSchool(
            @RequestBody com.cm.sanchalak.platform.onboarding.SchoolOnboardingRequest request) {
        return ResponseEntity.ok(schoolService.onboardSchool(request));
    }
}
