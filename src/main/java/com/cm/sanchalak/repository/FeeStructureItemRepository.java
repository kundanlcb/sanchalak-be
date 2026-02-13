package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.FeeStructureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeStructureItemRepository extends JpaRepository<FeeStructureItem, Long> {
    boolean existsByFeeCategoryId(Long categoryId);
}
