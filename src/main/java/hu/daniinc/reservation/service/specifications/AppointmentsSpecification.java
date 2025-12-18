package hu.daniinc.reservation.service.specifications;

import hu.daniinc.reservation.domain.Appointment;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public class AppointmentsSpecification {

    public static Specification<Appointment> overlappingAppointmentsByEmployeeName(
        Instant startDate,
        Instant endDate,
        Long businessId,
        String employeeFullName // pl. "John Doe"
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // dátum intervallum
            predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("startDate"), endDate));
            predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("endDate"), startDate));

            // business filter
            predicates = cb.and(predicates, cb.equal(root.get("businessEmployee").get("business").get("id"), businessId));

            // employee name filter
            if (employeeFullName != null && !employeeFullName.isEmpty()) {
                var firstNamePath = root.get("businessEmployee").get("user").get("firstName").as(String.class);
                var lastNamePath = root.get("businessEmployee").get("user").get("lastName").as(String.class);
                var fullNameExpr = cb.concat(cb.concat(firstNamePath, " "), lastNamePath);
                predicates = cb.and(predicates, cb.equal(fullNameExpr, employeeFullName));
            }

            // status filter
            predicates = cb.and(predicates, cb.notEqual(root.get("status"), "DELETED"));

            return predicates;
        };
    }
}
