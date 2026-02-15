package com.cm.sanchalak.platform.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolOperationRepository extends JpaRepository<SchoolOperationConfig, UUID> {
    Optional<SchoolOperationConfig> findBySchoolId(UUID schoolId);
}
