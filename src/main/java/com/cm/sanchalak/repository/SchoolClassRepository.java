package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findBySchoolId(UUID schoolId);

    java.util.Optional<SchoolClass> findByName(String name);
}
