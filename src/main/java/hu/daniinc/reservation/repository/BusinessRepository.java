package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Business;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Business entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Business b WHERE b.user.login = ?#{authentication.name}")
    Boolean hasLoggedInUserBusiness();

    @Query("select b from Business b where b.user.login = ?#{authentication.name}")
    Optional<Business> findByLogin();
}
