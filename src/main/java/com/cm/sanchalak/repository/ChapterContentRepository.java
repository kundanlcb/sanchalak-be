package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ChapterContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterContentRepository
                extends JpaRepository<ChapterContent, Long>, JpaSpecificationExecutor<ChapterContent> {
        List<ChapterContent> findByChapter_IdOrderBySequenceOrderAsc(Long chapterId);

}
