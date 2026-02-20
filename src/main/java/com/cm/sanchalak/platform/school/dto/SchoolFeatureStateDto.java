package com.cm.sanchalak.platform.school.dto;

import java.util.UUID;

public record SchoolFeatureStateDto(
        UUID featureId,
        String code,
        String name,
        String description,
        boolean inActivePlan,
        boolean assignedToSchool,
        boolean enabled) {
}
