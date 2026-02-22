package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Subject;
import org.springframework.data.jpa.domain.Specification;

public class SubjectSpecification extends BaseSpecification {
    public static Specification<Subject> activeScoped() {
        return Specification.where(scoped());
    }

    public static Specification<Subject> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
