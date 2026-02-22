package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.HomeworkSubmission;
import org.springframework.data.jpa.domain.Specification;

public class HomeworkSubmissionSpecification extends BaseSpecification {

    public static Specification<HomeworkSubmission> activeScoped() {
        return BaseSpecification.scoped("student");
    }

    public static Specification<HomeworkSubmission> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }
}
