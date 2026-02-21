package hu.daniinc.reservation.service.specifications;

import hu.daniinc.reservation.config.WebConfigurer;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

public class GuestSpecification {

    private static final Logger LOG = LoggerFactory.getLogger(GuestSpecification.class);

    public static Specification<Guest> filterForUser(String filter, BusinessEmployee currentEmployee, Long filterEmployeeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Guest, BusinessEmployee> employeeJoin = root.join("businessEmployee");

            predicates.add(cb.equal(employeeJoin.get("business"), currentEmployee.getBusiness()));

            if (currentEmployee.getPermissions().contains(BusinessPermission.VIEW_ALL_GUESTS)) {
                if (filterEmployeeId != null) {
                    predicates.add(cb.equal(employeeJoin.get("id"), filterEmployeeId));
                }
            } else {
                predicates.add(cb.equal(employeeJoin, currentEmployee));
                LOG.debug("Filtering allowed only for current logged in employee!");
            }

            if (filter != null && !filter.isBlank() && !"appointment-is-null".equals(filter)) {
                predicates.add(search(filter, root, cb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Guest> appointmentIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("appointment"));
    }

    private static Predicate search(String searchTerm, Root<Guest> root, CriteriaBuilder cb) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";

        return cb.or(
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(root.get("email")), pattern),
            cb.like(cb.lower(root.get("phoneNumber")), pattern)
        );
    }
}
