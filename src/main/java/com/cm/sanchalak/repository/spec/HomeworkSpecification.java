package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Homework;
import org.springframework.data.jpa.domain.Specification;

public class HomeworkSpecification extends BaseSpecification {

    public static Specification<Homework> activeScoped() {
        return BaseSpecification.scoped("studentClass");
    }

    public static Specification<Homework> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
