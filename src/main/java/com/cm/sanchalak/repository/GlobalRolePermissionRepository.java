package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.GlobalRolePermission;
import com.cm.sanchalak.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GlobalRolePermissionRepository extends JpaRepository<GlobalRolePermission, UUID> {
    List<GlobalRolePermission> findByRoleName(RoleName roleName);

    void deleteByRoleName(RoleName roleName);
}
