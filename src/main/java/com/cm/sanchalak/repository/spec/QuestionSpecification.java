package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Question;
import org.springframework.data.jpa.domain.Specification;

public class QuestionSpecification extends BaseSpecification {
    public static Specification<Question> activeScoped() {
        return BaseSpecification.scoped();
    }

    public static Specification<Question> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
