package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.ExamQuestion;
import org.springframework.data.jpa.domain.Specification;

public class ExamQuestionSpecification extends BaseSpecification {
    public static Specification<ExamQuestion> activeScoped() {
        return BaseSpecification.scoped("examSchedule");
    }

    public static Specification<ExamQuestion> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
