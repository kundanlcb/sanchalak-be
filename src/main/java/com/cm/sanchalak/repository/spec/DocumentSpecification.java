package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.StudentDocument;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecification extends BaseSpecification {

    public static Specification<StudentDocument> activeScoped() {
        return BaseSpecification.scoped("student");
    }

    public static Specification<StudentDocument> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
