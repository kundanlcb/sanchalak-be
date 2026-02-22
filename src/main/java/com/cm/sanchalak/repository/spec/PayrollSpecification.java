package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.PayrollRecord;
import org.springframework.data.jpa.domain.Specification;

public class PayrollSpecification extends BaseSpecification {

    public static Specification<PayrollRecord> activeScoped() {
        return BaseSpecification.scoped("teacher");
    }

    public static Specification<PayrollRecord> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
