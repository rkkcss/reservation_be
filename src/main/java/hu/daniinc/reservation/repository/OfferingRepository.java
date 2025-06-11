package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Offering entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {
    @Query("select o from Offering o where o.business.user.login = ?#{authentication.name}")
    Page<Offering> getAllByBusinessOwner(Pageable pageable);

    @Query("select o from Offering o where o.business.id = ?1")
    Page<Offering> findAllByBusinessId(Long id, Pageable pageable);

    @Query("select o from Offering o where o.business.user.login = ?#{authentication.name}")
    List<Offering> getAllByBusinessOwnerWithoutPagination();

    @Query("select o from Offering o where o.id = ?1 and o.business.user.login = ?#{authentication.name}")
    Optional<Offering> findByIdToBusiness(Long offeringId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END FROM Offering o WHERE o.id = ?1 and o.business.id = ?2")
    Boolean isBusinessHasTheOffer(Long offeringId, Long businessId);
}
