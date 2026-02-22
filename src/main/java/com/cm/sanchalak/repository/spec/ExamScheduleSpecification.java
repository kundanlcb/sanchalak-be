package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.ExamSchedule;
import org.springframework.data.jpa.domain.Specification;

public class ExamScheduleSpecification extends BaseSpecification {
    public static Specification<ExamSchedule> activeScoped() {
        return BaseSpecification.scoped("studentClass");
    }

    public static Specification<ExamSchedule> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }

    public static Specification<ExamSchedule> byTermAndClass(Long termId, Long classId) {
        return activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("examTerm").get("id"), termId))
                .and((root, query, cb) -> cb.equal(root.get("studentClass").get("id"), classId));
    }
}
