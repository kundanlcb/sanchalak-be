package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.security.SchoolContext;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class BaseSpecification {

    /**
     * Automatically applies school filtering if the user is not a platform admin.
     * If schoolId is passed explicitly (e.g. by an admin), it uses that.
     */
    public static <T> Specification<T> hasSchool(UUID schoolId) {
        return (root, query, cb) -> {
            if (schoolId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("schoolId"), schoolId);
        };
    }

    /**
     * Standardized multi-tenancy filter that respects the SecurityContext.
     */
    public static <T> Specification<T> scoped() {
        return (root, query, cb) -> {
            if (SchoolContext.isPlatformAdmin()) {
                return cb.conjunction();
            }
            UUID schoolId = SchoolContext.getSchoolId();
            if (schoolId == null) {
                return cb.disjunction();
            }
            return cb.equal(root.get("schoolId"), schoolId);
        };
    }

    /**
     * Multi-tenancy filter for entities that are scoped via an association (e.g.
     * ExamSchedule -> StudentClass).
     */
    public static <T> Specification<T> scoped(String associationPath) {
        return (root, query, cb) -> {
            if (SchoolContext.isPlatformAdmin()) {
                return cb.conjunction();
            }
            UUID schoolId = SchoolContext.getSchoolId();
            if (schoolId == null) {
                return cb.disjunction();
            }
            return cb.equal(root.get(associationPath).get("schoolId"), schoolId);
        };
    }
}
