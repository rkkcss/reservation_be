package hu.daniinc.reservation.service.specifications;

import hu.daniinc.reservation.domain.Notification;
import org.springframework.data.jpa.domain.Specification;

public final class NotificationSpecification {

    private NotificationSpecification() {}

    public static Specification<Notification> businessEmployeeEquals(Long businessEmployeeId) {
        return (root, query, cb) -> cb.equal(root.get("businessEmployee").get("id"), businessEmployeeId);
    }

    public static Specification<Notification> isUnread() {
        return (root, query, cb) -> cb.isFalse(root.get("read"));
    }
}
