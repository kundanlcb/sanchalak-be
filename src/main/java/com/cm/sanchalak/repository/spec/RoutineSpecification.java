package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.ClassRoutine;
import org.springframework.data.jpa.domain.Specification;

public class RoutineSpecification extends BaseSpecification {

    public static Specification<ClassRoutine> activeScoped() {
        return BaseSpecification.scoped("studentClass");
    }

    public static Specification<ClassRoutine> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
