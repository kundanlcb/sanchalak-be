package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.SchoolClass;
import org.springframework.data.jpa.domain.Specification;

public class SchoolClassSpecification extends BaseSpecification {
    public static Specification<SchoolClass> activeScoped() {
        return Specification.where(scoped());
    }

    public static Specification<SchoolClass> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
