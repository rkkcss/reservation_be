package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.BusinessOpeningHours;
import hu.daniinc.reservation.service.dto.BusinessOpeningHoursDTO;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BusinessOpeningHours entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BusinessOpeningHoursRepository extends JpaRepository<BusinessOpeningHours, Long> {
    @Query("select boo from BusinessOpeningHours boo where boo.business.id = :businessId")
    List<BusinessOpeningHours> findAllByBusinessId(@Param("businessId") Long businessId);
}
