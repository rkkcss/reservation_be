package hu.daniinc.reservation.service.specifications;

import hu.daniinc.reservation.domain.Appointment;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public class AppointmentsSpecification {

    public static Specification<Appointment> overlappingAppointmentsByEmployeeName(
        Instant startDate,
        Instant endDate,
        Long businessId,
        String employeeId
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // date range
            predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("startDate"), endDate));
            predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("endDate"), startDate));

            // business filter
            predicates = cb.and(predicates, cb.equal(root.get("businessEmployee").get("business").get("id"), businessId));

            // employee name filter
            if (employeeId != null && !employeeId.isEmpty() && !"all".equalsIgnoreCase(employeeId)) {
                var searchedEmployeeId = root.get("businessEmployee").get("id");
                predicates = cb.and(predicates, cb.equal(searchedEmployeeId, employeeId));
            }
            // if employeeFullName all -> dont add name filter -> return ALL the employees

            // status filter
            predicates = cb.and(predicates, cb.notEqual(root.get("status"), "DELETED"));

            return predicates;
        };
    }
}
