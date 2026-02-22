package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Notice;
import com.cm.sanchalak.entity.NoticeReadStatus;
import org.springframework.data.jpa.domain.Specification;

public class NoticeSpecification extends BaseSpecification {

    public static Specification<Notice> activeScoped() {
        return BaseSpecification.scoped();
    }

    public static Specification<Notice> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }

    public static Specification<NoticeReadStatus> readStatusScoped() {
        return BaseSpecification.scoped("notice");
    }
}
