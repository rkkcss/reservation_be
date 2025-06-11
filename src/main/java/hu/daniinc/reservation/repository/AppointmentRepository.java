package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Appointment;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Appointment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query(
        "SELECT a FROM Appointment a WHERE a.startDate <= :endDate AND a.endDate >= :startDate AND a.business.user.login = ?#{authentication.name}"
    )
    List<Appointment> findOverlappingAppointments(@Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate);

    @Query(
        "SELECT a FROM Appointment a WHERE a.business.id = :businessId AND a.startDate < :end AND a.endDate > :start AND a.status <> 'CANCELLED'"
    )
    List<Appointment> findByBusinessIdAndDateRange(
        @Param("businessId") Long businessId,
        @Param("start") ZonedDateTime start,
        @Param("end") ZonedDateTime end
    );

    @Query(
        "SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM Appointment a WHERE a.business.user.login = ?#{authentication.name} and a.id = ?1"
    )
    Boolean isBusinessTheOwnerById(Long id);
}
