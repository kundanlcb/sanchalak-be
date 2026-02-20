package com.cm.sanchalak.dto;

import com.cm.sanchalak.platform.school.SchoolRolePermission;
import java.util.List;

public record SchoolPermissionResponse(
        List<String> availableFeatures,
        List<SchoolRolePermission> rolePermissions) {
}
