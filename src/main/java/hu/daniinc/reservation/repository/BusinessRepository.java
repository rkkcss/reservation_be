package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Business;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Business entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Business b WHERE b.owner.login = ?#{authentication.name}")
    Boolean hasLoggedInUserBusiness();

    @Query(
        "SELECT b " +
        "FROM Business b " +
        "JOIN b.businessEmployees be " +
        "WHERE b.id = :businessId " +
        "AND be.user.login = ?#{authentication.name}"
    )
    Optional<Business> findBusinessByLoginAndBusinessId(@Param(value = "businessId") Long businessId);
}
