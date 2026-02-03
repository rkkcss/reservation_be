package hu.daniinc.reservation.service.specifications;

import hu.daniinc.reservation.config.WebConfigurer;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

public class GuestSpecification {

    private static final Logger LOG = LoggerFactory.getLogger(GuestSpecification.class);

    public static Specification<Guest> filterForUser(BusinessEmployee currentEmployee, Long filterEmployeeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Join a Guest -> BusinessEmployee kapcsolaton
            // Feltételezve, hogy a Guest entitásban a mező neve 'employee' vagy 'businessEmployee'
            Join<Guest, BusinessEmployee> employeeJoin = root.join("businessEmployee");

            // 2. Szűrés a Business-re az összekapcsolt táblán keresztül
            // Guest -> BusinessEmployee -> Business
            predicates.add(cb.equal(employeeJoin.get("business"), currentEmployee.getBusiness()));

            // 3. Jogosultság alapú szűrés
            if (currentEmployee.getPermissions().contains(BusinessPermission.VIEW_ALL_GUESTS)) {
                // Ha van joga mindent látni, és van konkrét szűrőnk
                if (filterEmployeeId != null) {
                    predicates.add(cb.equal(employeeJoin.get("id"), filterEmployeeId));
                }
            } else {
                // Ha nincs joga, akkor a Guest-hez rendelt BusinessEmployee-nak
                // meg kell egyeznie a bejelentkezett BusinessEmployee-val
                predicates.add(cb.equal(employeeJoin, currentEmployee));
                LOG.debug("Filtering allowed only for current logged in employee!");
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Guest> appointmentIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("appointment"));
    }
}
