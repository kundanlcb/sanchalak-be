package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.TransportEvent;
import org.springframework.data.jpa.domain.Specification;

public class TransportEventSpecification extends BaseSpecification {

    public static Specification<TransportEvent> activeScoped() {
        return BaseSpecification.scoped("student");
    }

    public static Specification<TransportEvent> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
