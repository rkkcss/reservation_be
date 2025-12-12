package hu.daniinc.reservation.service.specifications;

import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class OfferingSpecification {

    public static Specification<Offering> publicOfferingsWithEmployeeNameFilter(Long businessId, String search) {
        return Specification.where(belongsToBusinessId(businessId)).and(isPublicOffering()).and(searchByEmployeeName(search));
    }

    private static Specification<Offering> belongsToBusinessId(Long businessId) {
        return (root, query, cb) -> cb.equal(root.get("businessEmployee").get("business").get("id"), businessId);
    }

    private static Specification<Offering> isPublicOffering() {
        return (root, query, cb) -> cb.and(cb.equal(root.get("status"), BasicEntityStatus.ACTIVE));
    }

    private static Specification<Offering> searchByEmployeeName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String pattern = "%" + search.toLowerCase() + "%";

            Join<Object, Object> businessEmployee = root.join("businessEmployee");
            Join<Object, Object> user = businessEmployee.join("user");

            Expression<String> fullName = cb.concat(cb.concat(cb.lower(user.get("firstName")), " "), cb.lower(user.get("lastName")));

            return cb.or(
                cb.like(cb.lower(user.get("firstName")), pattern),
                cb.like(cb.lower(user.get("lastName")), pattern),
                cb.like(fullName, pattern)
            );
        };
    }
}
