package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.BusinessRating;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BusinessRating entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BusinessRatingRepository extends JpaRepository<BusinessRating, Long> {}
