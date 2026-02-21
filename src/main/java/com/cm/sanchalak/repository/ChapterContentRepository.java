package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ChapterContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterContentRepository extends JpaRepository<ChapterContent, Long> {
        List<ChapterContent> findByChapter_IdOrderBySequenceOrderAsc(Long chapterId);

        @Query("SELECT c FROM ChapterContent c WHERE " +
                        "(:classId IS NULL OR c.chapter.schoolClass.id = :classId) AND " +
                        "(:subjectId IS NULL OR c.chapter.subject.id = :subjectId) AND " +
                        "(:chapterId IS NULL OR c.chapter.id = :chapterId) AND " +
                        "(:contentType IS NULL OR c.contentType = :contentType) " +
                        "ORDER BY c.chapter.sequenceOrder ASC, c.sequenceOrder ASC")
        Page<ChapterContent> findAllWithFilters(
                        @Param("classId") Long classId,
                        @Param("subjectId") Long subjectId,
                        @Param("chapterId") Long chapterId,
                        @Param("contentType") ChapterContent.ContentType contentType,
                        Pageable pageable);
}
