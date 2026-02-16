package com.cm.sanchalak.platform.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolSubscriptionRepository extends JpaRepository<SchoolSubscription, UUID> {
    List<SchoolSubscription> findBySchoolId(UUID schoolId);

    Optional<SchoolSubscription> findBySchoolIdAndStatus(UUID schoolId, SubscriptionStatus status);
}
