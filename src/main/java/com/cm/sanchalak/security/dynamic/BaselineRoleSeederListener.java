package com.cm.sanchalak.security.dynamic;

import com.cm.sanchalak.entity.ApiEndpoint;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.repository.ApiEndpointRepository;
import com.cm.sanchalak.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Order(100) // Ensure this runs AFTER the EndpointScannerListener
@RequiredArgsConstructor
public class BaselineRoleSeederListener implements ApplicationListener<ApplicationReadyEvent> {

    private final ApiEndpointRepository apiEndpointRepository;
    private final RoleRepository roleRepository;
    private final DynamicAuthCacheService dynamicAuthCacheService;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Starting Baseline Role Seeder for Dynamic Auth...");

        Optional<Role> adminRoleOpt = roleRepository.findByName(RoleName.ROLE_ADMIN);
        Optional<Role> schoolAdminRoleOpt = roleRepository.findByName(RoleName.ROLE_SCHOOL_ADMIN);

        if (adminRoleOpt.isEmpty()) {
            log.warn("ROLE_ADMIN not found! Baseline seeder cannot map default permissions.");
            return;
        }

        Role adminRole = adminRoleOpt.get();
        Role schoolAdminRole = schoolAdminRoleOpt.orElse(null);

        List<ApiEndpoint> allEndpoints = apiEndpointRepository.findAll();
        boolean mappingsChanged = false;

        for (ApiEndpoint endpoint : allEndpoints) {
            // As a baseline migration strategy, assign ROLE_ADMIN to EVERY endpoint
            // so that the admin can log in and use the UI to configure other roles.
            if (!endpoint.getRoles().contains(adminRole)) {
                endpoint.getRoles().add(adminRole);
                mappingsChanged = true;
            }

            // Grant default access to ROLE_SCHOOL_ADMIN for all endpoints to allow complete
            // access to sanchalan
            if (schoolAdminRole != null) {
                if (!endpoint.getRoles().contains(schoolAdminRole)) {
                    endpoint.getRoles().add(schoolAdminRole);
                    mappingsChanged = true;
                }
            }
        }

        if (mappingsChanged) {
            log.info("Baseline Admin mappings applied. Saving to database...");
            apiEndpointRepository.saveAll(allEndpoints);

            // Force the cache to reload now that the database has the mappings
            dynamicAuthCacheService.refreshCache();
        }

        log.info("Finished Baseline Role Seeder.");
    }
}
