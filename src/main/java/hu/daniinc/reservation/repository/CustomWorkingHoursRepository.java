package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.CustomWorkingHours;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CustomWorkingHours entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomWorkingHoursRepository extends JpaRepository<CustomWorkingHours, Long> {
    Optional<CustomWorkingHours> findByBusinessIdAndWorkDate(Long businessId, LocalDate date);

    //TODO: fix it
    @Query(
        "SELECT c FROM CustomWorkingHours c WHERE c.business.id = :businessId AND c.business.owner.id = :employeeId AND c.workDate = :date"
    )
    Optional<CustomWorkingHours> findByBusinessIdAndEmployeeIdAndWorkDate(Long businessId, Long employeeId, LocalDate date);
}
