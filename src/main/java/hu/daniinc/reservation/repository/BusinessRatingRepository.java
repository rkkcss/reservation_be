package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.BusinessRating;
import hu.daniinc.reservation.service.dto.BusinessRatingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BusinessRating entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BusinessRatingRepository extends JpaRepository<BusinessRating, Long> {
    @Query("select br from BusinessRating br where br.business.id = ?1")
    Page<BusinessRating> findAllByBusinessId(Long businessId, Pageable pageable);
}
