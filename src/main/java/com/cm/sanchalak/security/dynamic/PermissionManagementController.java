package com.cm.sanchalak.security.dynamic;

import com.cm.sanchalak.entity.ApiEndpoint;
import com.cm.sanchalak.entity.GlobalRolePermission;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.platform.subscription.Feature;
import com.cm.sanchalak.platform.subscription.FeatureRepository;
import com.cm.sanchalak.repository.ApiEndpointRepository;
import com.cm.sanchalak.repository.GlobalRolePermissionRepository;
import com.cm.sanchalak.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/permissions")
@RequiredArgsConstructor
public class PermissionManagementController {

    private final ApiEndpointRepository apiEndpointRepository;
    private final RoleRepository roleRepository;
    private final DynamicAuthCacheService dynamicAuthCacheService;
    private final FeatureRepository featureRepository;
    private final GlobalRolePermissionRepository globalRolePermissionRepository;

    /**
     * Get all discovered endpoints, usually used to build the UI checklist grid.
     */
    @GetMapping("/endpoints")
    public ResponseEntity<List<ApiEndpoint>> getAllEndpoints() {
        return ResponseEntity.ok(apiEndpointRepository.findAll());
    }

    /**
     * Get all roles available in the system.
     */
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    /**
     * Get the list of endpoint IDs currently mapped to a specific role.
     */
    @GetMapping("/roles/{roleId}/endpoints")
    public ResponseEntity<List<Long>> getEndpointsForRole(@PathVariable Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Long> endpointIds = apiEndpointRepository.findAll().stream()
                .filter(endpoint -> endpoint.getRoles().contains(role))
                .map(ApiEndpoint::getId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(endpointIds);
    }

    /**
     * Update the mappings for a specific role.
     * Replaces the current mappings entirely with the provided list of endpoint
     * IDs.
     */
    @PostMapping("/roles/{roleId}/endpoints")
    public ResponseEntity<Void> updateRoleEndpoints(@PathVariable Long roleId, @RequestBody List<Long> endpointIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // 1. Remove this role from all endpoints currently
        List<ApiEndpoint> allEndpoints = apiEndpointRepository.findAll();
        for (ApiEndpoint endpoint : allEndpoints) {
            endpoint.getRoles().remove(role);
        }

        // 2. Add this role to the newly selected endpoints
        if (endpointIds != null && !endpointIds.isEmpty()) {
            List<ApiEndpoint> selectedEndpoints = apiEndpointRepository.findAllById(endpointIds);
            for (ApiEndpoint endpoint : selectedEndpoints) {
                endpoint.getRoles().add(role);
            }
        }

        // 3. Save all changes
        apiEndpointRepository.saveAll(allEndpoints);

        // 4. CRITICAL: Invalidate the in-memory cache so permissions update instantly
        dynamicAuthCacheService.refreshCache();

        return ResponseEntity.ok().build();
    }

    /**
     * Get all master features.
     */
    @GetMapping("/features")
    public ResponseEntity<List<Feature>> getAllFeatures() {
        return ResponseEntity.ok(featureRepository.findAll());
    }

    /**
     * Get feature codes for a specific role globally.
     */
    @GetMapping("/roles/{roleId}/features")
    public ResponseEntity<List<String>> getFeaturesForRole(@PathVariable Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<String> featureCodes = globalRolePermissionRepository.findByRoleName(role.getName()).stream()
                .map(GlobalRolePermission::getFeatureCode)
                .collect(Collectors.toList());

        return ResponseEntity.ok(featureCodes);
    }

    /**
     * Update global feature mappings for a role.
     */
    @PostMapping("/roles/{roleId}/features")
    @Transactional
    public ResponseEntity<Void> updateRoleFeatures(@PathVariable Long roleId, @RequestBody List<String> featureCodes) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        globalRolePermissionRepository.deleteByRoleName(role.getName());

        if (featureCodes != null) {
            List<GlobalRolePermission> newPermissions = featureCodes.stream()
                    .map(code -> GlobalRolePermission.builder()
                            .roleName(role.getName())
                            .featureCode(code)
                            .build())
                    .collect(Collectors.toList());
            globalRolePermissionRepository.saveAll(newPermissions);
        }

        return ResponseEntity.ok().build();
    }
}
