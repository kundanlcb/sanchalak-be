package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.*;
import org.springframework.data.jpa.domain.Specification;

public class TransportSpecification extends BaseSpecification {

    public static Specification<Route> routeScoped() {
        return BaseSpecification.scoped();
    }

    public static Specification<Route> routeById(Long id) {
        return routeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }

    public static Specification<Vehicle> vehicleScoped() {
        return BaseSpecification.scoped();
    }

    public static Specification<Vehicle> vehicleById(Long id) {
        return vehicleScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }

    public static Specification<Stop> stopScoped() {
        return BaseSpecification.scoped("route");
    }

    public static Specification<Stop> stopById(Long id) {
        return stopScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }

    public static Specification<Trip> tripScoped() {
        return BaseSpecification.scoped("route");
    }

    public static Specification<StudentTransportAssignment> assignmentScoped() {
        return BaseSpecification.scoped("route");
    }
}
