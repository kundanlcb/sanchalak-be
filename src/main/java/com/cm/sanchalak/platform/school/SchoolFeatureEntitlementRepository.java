package com.cm.sanchalak.platform.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolFeatureEntitlementRepository extends JpaRepository<SchoolFeatureEntitlement, UUID> {
    List<SchoolFeatureEntitlement> findBySchoolId(UUID schoolId);

    List<SchoolFeatureEntitlement> findBySchoolIdAndEnabledTrue(UUID schoolId);

    Optional<SchoolFeatureEntitlement> findBySchoolIdAndFeature_Id(UUID schoolId, UUID featureId);
}
