package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.WorkingHours;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WorkingHours entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    @Query(
        "select wo from WorkingHours wo where wo.businessEmployee.user.login = ?#{authentication.name} and wo.businessEmployee.business.id = :businessId"
    )
    List<WorkingHours> findByBusinessUserLogin(@Param("businessId") Long businessId);

    @Query(
        "SELECT w FROM WorkingHours w WHERE w.businessEmployee.business.id = :businessId AND w.businessEmployee.user.id = :employeeId AND w.dayOfWeek = :dayOfWeek"
    )
    List<WorkingHours> findByBusinessIdAndEmployeeIdAndDayOfWeek(
        @Param("businessId") Long businessId,
        @Param("employeeId") Long employeeId,
        @Param("dayOfWeek") Integer dayOfWeek
    );

    @Query("select w from WorkingHours w where w.businessEmployee.business.id = :businessId and w.businessEmployee.user.id = :employeeId")
    Set<WorkingHours> findAllByBusinessAndEmployeeId(@Param("businessId") Long businessId, @Param("employeeId") Long employeeId);
}
