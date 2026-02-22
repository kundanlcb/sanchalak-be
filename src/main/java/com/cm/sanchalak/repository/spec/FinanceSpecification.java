package com.cm.sanchalak.repository.spec;

import org.springframework.data.jpa.domain.Specification;

public class FinanceSpecification extends BaseSpecification {

    public static <T> Specification<T> activeScoped() {
        return BaseSpecification.scoped();
    }

    public static <T> Specification<T> activeById(Long id) {
        Specification<T> spec = activeScoped();
        return spec.and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
