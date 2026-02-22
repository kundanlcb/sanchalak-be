package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.ParentStudentLink;
import org.springframework.data.jpa.domain.Specification;

public class ParentStudentLinkSpecification extends BaseSpecification {

    public static Specification<ParentStudentLink> activeScoped() {
        return BaseSpecification.scoped("student");
    }

    public static Specification<ParentStudentLink> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
