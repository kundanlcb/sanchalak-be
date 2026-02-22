package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.ExamTerm;
import org.springframework.data.jpa.domain.Specification;

public class ExamTermSpecification extends BaseSpecification {
    public static Specification<ExamTerm> activeScoped() {
        return Specification.where(scoped());
    }

    public static Specification<ExamTerm> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
