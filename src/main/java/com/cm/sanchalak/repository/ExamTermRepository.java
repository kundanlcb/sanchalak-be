package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ExamTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamTermRepository extends JpaRepository<ExamTerm, Long>, JpaSpecificationExecutor<ExamTerm> {
    List<ExamTerm> findBySchoolId(UUID schoolId);
}
