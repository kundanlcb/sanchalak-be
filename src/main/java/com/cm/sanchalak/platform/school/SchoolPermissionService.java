package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.entity.RoleName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SchoolPermissionService {

    private final SchoolPermissionRepository repository;

    public SchoolPermissionService(SchoolPermissionRepository repository) {
        this.repository = repository;
    }

    public List<SchoolRolePermission> getPermissionsBySchool(UUID schoolId) {
        return repository.findBySchoolId(schoolId);
    }

    @Transactional
    public void updateRolePermissions(UUID schoolId, RoleName roleName, List<String> featureCodes) {
        repository.deleteBySchoolIdAndRoleName(schoolId, roleName);

        List<SchoolRolePermission> permissions = featureCodes.stream()
                .map(code -> SchoolRolePermission.builder()
                        .schoolId(schoolId)
                        .roleName(roleName)
                        .featureCode(code)
                        .build())
                .collect(Collectors.toList());

        repository.saveAll(permissions);
    }
}
