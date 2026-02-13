package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long> {
    boolean existsByName(String name);
}
