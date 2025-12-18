package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.service.dto.AppointmentDTO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Appointment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
    @Query(
        "SELECT a FROM Appointment a " +
        "WHERE a.startDate <= :endDate " +
        "AND a.endDate >= :startDate " +
        "AND a.businessEmployee.user.id = :employeeId " +
        "AND a.businessEmployee.business.id = :businessId " +
        "and a.status <> 'DELETED'"
    )
    List<Appointment> findOverlappingAppointments(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("businessId") Long businessId,
        @Param("employeeId") Long employeeId
    );

    @Query(
        "SELECT a FROM Appointment a WHERE a.businessEmployee.business.id = :businessId AND a.startDate < :end AND a.endDate > :start and a.status <> 'DELETED'"
    )
    List<Appointment> findByBusinessIdAndDateRange(
        @Param("businessId") Long businessId,
        @Param("start") ZonedDateTime start,
        @Param("end") ZonedDateTime end
    );

    @Query(
        "SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM Appointment a WHERE a.businessEmployee.user.login = ?#{authentication.name} and a.id = ?1"
    )
    Boolean isUserTheAppointmentOwnerById(Long id);

    @Query(
        "select a from Appointment a where a.businessEmployee.user.login = ?#{authentication.name} and a.status = 'PENDING' and a.startDate >= CURRENT_DATE and a.businessEmployee.business.id = :businessId"
    )
    List<Appointment> findAllPendingAppointments(@Param("businessId") Long businessId);

    @Query("select a from Appointment a where a.modifierToken = ?1")
    Optional<Appointment> findByModifierToken(String modifierToken);

    @Query("select a from Appointment a where a.id = ?1 and a.businessEmployee.user.id = :employeeId")
    Optional<Appointment> findByIdAndLoggedInOwner(Long appointmentId, Long employeeId);

    @Query(
        "SELECT a FROM Appointment a " +
        "WHERE a.businessEmployee.business.id = :businessId " +
        "AND (:employeeId IS NULL OR a.businessEmployee.user.id = :employeeId) " +
        "AND a.startDate >= :startDate AND a.endDate <= :endDate"
    )
    List<Appointment> findByBusinessIdAndEmployeeIdAndDateRange(
        @Param("businessId") Long businessId,
        @Param("employeeId") Long employeeId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}
