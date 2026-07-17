package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.CustomWorkingHours;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CustomWorkingHours entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomWorkingHoursRepository extends JpaRepository<CustomWorkingHours, Long> {
    Optional<CustomWorkingHours> findByBusinessEmployeeIdAndWorkDate(Long businessId, LocalDate date);

    @Query(
        "SELECT c FROM CustomWorkingHours c WHERE c.businessEmployee.business.id = :businessId AND c.businessEmployee.id = :employeeId AND c.workDate = :date"
    )
    Optional<CustomWorkingHours> findByBusinessIdAndEmployeeIdAndWorkDate(Long businessId, Long employeeId, LocalDate date);

    @Query(
        "SELECT c FROM CustomWorkingHours c WHERE c.businessEmployee.business.id = :businessId " +
        "AND c.businessEmployee.user.id = :employeeId AND c.workDate BETWEEN :from AND :to"
    )
    List<CustomWorkingHours> findByBusinessIdAndEmployeeIdAndWorkDateBetween(
        @Param("businessId") Long businessId,
        @Param("employeeId") Long employeeId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
    //List<CustomWorkingHours> findByBusinessIdAndEmployeeIdAndWorkDateBetween(Long businessId, Long employeeId, LocalDate from, LocalDate to);
}
