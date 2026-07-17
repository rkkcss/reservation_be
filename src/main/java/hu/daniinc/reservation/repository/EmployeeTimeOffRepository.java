package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.EmployeeTimeOff;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeTimeOffRepository extends JpaRepository<EmployeeTimeOff, Long> {
    @Query(
        "SELECT t FROM EmployeeTimeOff t " +
        "LEFT JOIN FETCH t.businessEmployee be " +
        "LEFT JOIN FETCH be.workingHours " +
        "WHERE be.business.id = :businessId " +
        "AND (:employeeId IS NULL OR be.user.id = :employeeId) " +
        "AND t.status = hu.daniinc.reservation.domain.enumeration.TimeOffStatus.ACTIVE " +
        "AND t.startInstant < :rangeEnd AND t.endInstant > :rangeStart " +
        "ORDER BY t.startInstant ASC"
    )
    List<EmployeeTimeOff> findOverlapping(
        @Param("businessId") Long businessId,
        @Param("employeeId") Long employeeId,
        @Param("rangeStart") Instant rangeStart,
        @Param("rangeEnd") Instant rangeEnd
    );

    @Query(
        "SELECT t FROM EmployeeTimeOff t " +
        "WHERE t.businessEmployee.business.id = :businessId " +
        "AND t.businessEmployee.user.id = :employeeId " +
        "ORDER BY t.startDate DESC"
    )
    List<EmployeeTimeOff> findAllByBusinessAndEmployee(@Param("businessId") Long businessId, @Param("employeeId") Long employeeId);

    @Query("SELECT t FROM EmployeeTimeOff t " + "WHERE t.id = :id AND t.businessEmployee.business.id = :businessId")
    Optional<EmployeeTimeOff> findByIdAndBusinessId(@Param("id") Long id, @Param("businessId") Long businessId);
}
