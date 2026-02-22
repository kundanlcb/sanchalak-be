package com.cm.sanchalak.security;

import com.cm.sanchalak.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class OwnershipValidator {

    /**
     * Checks if the current user has access to a resource with the given schoolId.
     * Throws AppException if access is denied.
     */
    public void validate(UUID resourceSchoolId) {
        if (SchoolContext.isPlatformAdmin()) {
            return;
        }

        UUID currentUserSchoolId = SchoolContext.getSchoolId();
        if (currentUserSchoolId == null || !currentUserSchoolId.equals(resourceSchoolId)) {
            log.warn("Ownership validation failed. User School: {}, Resource School: {}",
                    currentUserSchoolId, resourceSchoolId);
            throw new AppException("Unauthorized access to resource");
        }
    }

    /**
     * Generic validation for any object that has a school ID.
     * This can be extended to support more entity types.
     */
    public void validate(Object entity) {
        if (SchoolContext.isPlatformAdmin()) {
            return;
        }

        try {
            // Reflectively check for getSchoolId() if we don't want to use an interface
            var method = entity.getClass().getMethod("getSchoolId");
            UUID schoolId = (UUID) method.invoke(entity);
            validate(schoolId);
        } catch (Exception e) {
            log.error("Failed to extract schoolId from entity of type {}", entity.getClass().getName());
            throw new AppException("Internal security error during ownership validation");
        }
    }
}
