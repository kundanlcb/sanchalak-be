package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchoolPermissionRepository extends JpaRepository<SchoolRolePermission, UUID> {
    List<SchoolRolePermission> findBySchoolId(UUID schoolId);

    void deleteBySchoolIdAndRoleName(UUID schoolId, RoleName roleName);
}
