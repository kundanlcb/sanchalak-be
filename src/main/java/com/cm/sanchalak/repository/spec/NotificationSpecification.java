package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.NotificationLog;
import com.cm.sanchalak.entity.NotificationToken;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecification extends BaseSpecification {

    public static Specification<NotificationLog> logScoped() {
        return BaseSpecification.scoped("user");
    }

    public static Specification<NotificationToken> tokenScoped() {
        return BaseSpecification.scoped("user");
    }
}
