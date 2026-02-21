package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.dto.SchoolPermissionResponse;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.platform.school.SchoolPermissionService;
import com.cm.sanchalak.platform.school.SchoolRolePermission;
import com.cm.sanchalak.platform.school.SchoolUserRepository;
import com.cm.sanchalak.platform.subscription.SubscriptionService;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/school/permissions")
public class SchoolPermissionController {

    private final SchoolPermissionService service;
    private final SchoolUserRepository schoolUserRepository;

    private final SubscriptionService subscriptionService;

    public SchoolPermissionController(SchoolPermissionService service, SchoolUserRepository schoolUserRepository,
            SubscriptionService subscriptionService) {
        this.service = service;
        this.schoolUserRepository = schoolUserRepository;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolPermissionResponse> getSchoolPermissions(
            @CurrentUser UserPrincipal currentUser) {
        UUID schoolId = schoolUserRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("School context not found"))
                .getSchoolId();

        List<String> availableFeatures = subscriptionService.getActiveSubscriptionFeatures(schoolId);
        List<SchoolRolePermission> rolePermissions = service.getPermissionsBySchool(schoolId);

        return ResponseEntity.ok(new SchoolPermissionResponse(availableFeatures, rolePermissions));
    }

    @PostMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateRolePermissions(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable RoleName roleName,
            @RequestBody List<String> featureCodes) {

        UUID schoolId = schoolUserRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("School context not found"))
                .getSchoolId();

        service.updateRolePermissions(schoolId, roleName, featureCodes);
        return ResponseEntity.ok().build();
    }
}
