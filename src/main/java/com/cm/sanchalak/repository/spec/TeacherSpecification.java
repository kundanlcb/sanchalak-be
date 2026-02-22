package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class TeacherSpecification extends BaseSpecification {

    public static Specification<Teacher> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Teacher> hasId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Teacher> activeScoped() {
        return Specification.where(isNotDeleted()).and(scoped());
    }

    public static Specification<Teacher> activeById(Long id) {
        return activeScoped().and(hasId(id));
    }
}
