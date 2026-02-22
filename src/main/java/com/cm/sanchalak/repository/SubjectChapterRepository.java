package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.SubjectChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectChapterRepository
        extends JpaRepository<SubjectChapter, Long>, JpaSpecificationExecutor<SubjectChapter> {
    List<SubjectChapter> findBySchoolClass_IdAndSubject_IdOrderBySequenceOrderAsc(Long classId, Long subjectId);
}
