package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Parent;
import org.springframework.data.jpa.domain.Specification;

public class ParentSpecification extends BaseSpecification {

    public static Specification<Parent> activeScoped() {
        return BaseSpecification.scoped();
    }

    public static Specification<Parent> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
