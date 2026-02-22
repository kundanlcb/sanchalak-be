package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
        List<Question> findByChapter_Id(Long chapterId);

        @Query("SELECT q FROM Question q WHERE " +
                        "(:classId IS NULL OR q.chapter.schoolClass.id = :classId) AND " +
                        "(:subjectId IS NULL OR q.chapter.subject.id = :subjectId) AND " +
                        "(:chapterId IS NULL OR q.chapter.id = :chapterId) " +
                        "ORDER BY q.chapter.sequenceOrder ASC")
        Page<Question> findAllWithFilters(
                        @Param("classId") Long classId,
                        @Param("subjectId") Long subjectId,
                        @Param("chapterId") Long chapterId,
                        Pageable pageable);
}
