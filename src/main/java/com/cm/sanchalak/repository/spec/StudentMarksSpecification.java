package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.StudentMarks;
import org.springframework.data.jpa.domain.Specification;

public class StudentMarksSpecification extends BaseSpecification {

    public static Specification<StudentMarks> activeScoped() {
        return BaseSpecification.scoped("student");
    }

    public static Specification<StudentMarks> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
