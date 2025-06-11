package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.BusinessOpeningHours;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BusinessOpeningHours entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BusinessOpeningHoursRepository extends JpaRepository<BusinessOpeningHours, Long> {}
