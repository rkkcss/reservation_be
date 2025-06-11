package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.WorkingHours;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WorkingHours entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    @Query("select wo from WorkingHours wo where wo.business.user.login = ?#{authentication.name}")
    List<WorkingHours> findByBusinessUserLogin();

    @Query("SELECT w FROM WorkingHours w WHERE w.business.id = :businessId AND w.dayOfWeek = :dayOfWeek")
    List<WorkingHours> findByBusinessIdAndDayOfWeek(@Param("businessId") Long businessId, @Param("dayOfWeek") Integer dayOfWeek);
}
