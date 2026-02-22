package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.*;
import org.springframework.data.jpa.domain.Specification;

public class CurriculumSpecification extends BaseSpecification {

    public static Specification<SubjectChapter> chapterScoped() {
        return BaseSpecification.scoped("schoolClass");
    }

    public static Specification<SubjectChapter> chapterById(Long id) {
        return chapterScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }

    public static Specification<ChapterContent> contentScoped() {
        return BaseSpecification.scoped();
    }

    public static Specification<ChapterContent> contentById(Long id) {
        return contentScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
