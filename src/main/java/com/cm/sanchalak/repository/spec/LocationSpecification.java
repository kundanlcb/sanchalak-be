package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.LocationPing;
import org.springframework.data.jpa.domain.Specification;

public class LocationSpecification extends BaseSpecification {

    public static Specification<LocationPing> activeScoped() {
        return BaseSpecification.scoped("vehicle");
    }

    public static Specification<LocationPing> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
