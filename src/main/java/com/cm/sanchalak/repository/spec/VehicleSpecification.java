package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Vehicle;
import org.springframework.data.jpa.domain.Specification;

public class VehicleSpecification extends BaseSpecification {

    public static Specification<Vehicle> activeScoped() {
        return BaseSpecification.scoped();
    }

    public static Specification<Vehicle> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
