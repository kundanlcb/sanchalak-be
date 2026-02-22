package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long>, JpaSpecificationExecutor<FeeCategory> {
    List<FeeCategory> findBySchoolId(UUID schoolId);

    boolean existsByNameAndSchoolId(String name, UUID schoolId);
}
