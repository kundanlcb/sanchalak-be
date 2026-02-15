package com.cm.sanchalak.platform.importing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {
    List<ImportJob> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId);
}
