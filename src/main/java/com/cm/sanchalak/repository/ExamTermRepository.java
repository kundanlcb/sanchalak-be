package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ExamTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamTermRepository extends JpaRepository<ExamTerm, Long> {
}
