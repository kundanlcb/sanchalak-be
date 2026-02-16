package com.cm.sanchalak.platform.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchoolUserRepository extends JpaRepository<SchoolUser, UUID> {
    List<SchoolUser> findBySchoolId(UUID schoolId);

    boolean existsBySchoolIdAndUserId(UUID schoolId, UUID userId);

    boolean existsBySchoolId(UUID schoolId);
}
