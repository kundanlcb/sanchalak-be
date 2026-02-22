package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long>, JpaSpecificationExecutor<SchoolClass> {
    List<SchoolClass> findBySchoolId(UUID schoolId);

    Optional<SchoolClass> findByName(String name);
}
