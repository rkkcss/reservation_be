package hu.daniinc.reservation.service.specifications;

import hu.daniinc.reservation.domain.Appointment;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public class AppointmentsSpecification {

    public static Specification<Appointment> overlappingAppointmentsByEmployeeName(
        Instant startDate,
        Instant endDate,
        Long businessId,
        String employeeFullName // f.e. "John Doe" or "all"
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // date range
            predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("startDate"), endDate));
            predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("endDate"), startDate));

            // business filter
            predicates = cb.and(predicates, cb.equal(root.get("businessEmployee").get("business").get("id"), businessId));

            // employee name filter
            if (employeeFullName != null && !employeeFullName.isEmpty() && !"all".equalsIgnoreCase(employeeFullName)) {
                var firstNamePath = root.get("businessEmployee").get("user").get("firstName").as(String.class);
                var lastNamePath = root.get("businessEmployee").get("user").get("lastName").as(String.class);
                var fullNameExpr = cb.concat(cb.concat(firstNamePath, " "), lastNamePath);
                predicates = cb.and(predicates, cb.equal(fullNameExpr, employeeFullName));
            }
            // if employeeFullName all -> dont add name filter -> return ALL the employees

            // status filter
            predicates = cb.and(predicates, cb.notEqual(root.get("status"), "DELETED"));

            return predicates;
        };
    }
}
