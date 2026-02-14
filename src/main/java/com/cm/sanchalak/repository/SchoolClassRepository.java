package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    java.util.Optional<SchoolClass> findByName(String name);
}
