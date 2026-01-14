package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.service.dto.RatingProjection;
import hu.daniinc.reservation.service.dto.RevenueProjection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticRepository extends JpaRepository<Appointment, Long> {
    //revenue and count it
    @Query(
        """
            SELECT new hu.daniinc.reservation.service.dto.RevenueProjection(
                SUM(a.offering.price),
                COUNT(a.id)
            )
            FROM Appointment a
            WHERE a.businessEmployee.business.id = :businessId
            AND a.createdDate BETWEEN :from AND :to
            AND a.status != 'CANCELLED'
            AND a.status != 'PENDING'
            AND (:employeeId IS NULL OR a.businessEmployee.user.id = :employeeId)
        """
    )
    RevenueProjection getRevenueAndCount(
        @Param("businessId") Long businessId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("employeeId") Long employeeId
    );

    // new customer count
    @Query(
        """
            SELECT COUNT(g.id)
            FROM Guest g
            WHERE g.businessEmployee.business.id = :businessId
            AND g.createdDate BETWEEN :from AND :to
            AND (:employeeId IS NULL OR g.businessEmployee.user.id = :employeeId)
        """
    )
    Long countNewCustomers(
        @Param("businessId") Long businessId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("employeeId") Long employeeId
    );

    // average rating and count
    @Query(
        """
            SELECT new hu.daniinc.reservation.service.dto.RatingProjection(
                AVG(r.number),
                COUNT(r.id)
            )
            FROM BusinessRating r
            WHERE r.business.id = :businessId
            AND r.createdDate BETWEEN :from AND :to
        """
    )
    RatingProjection getRatingSummary(@Param("businessId") Long businessId, @Param("from") Instant from, @Param("to") Instant to);

    //
    @Query(
        """
            SELECT COUNT(DISTINCT a.guest.id)
            FROM Appointment a
            WHERE a.businessEmployee.business.id = :businessId
            AND (:employeeId IS NULL OR a.businessEmployee.user.id = :employeeId)
            AND a.createdDate BETWEEN :from AND :to
        """
    )
    Long countUniqueCustomers(Long businessId, Long employeeId, Instant from, Instant to);

    // returning guests
    @Query(
        """
            SELECT COUNT(DISTINCT a.guest.id)
             FROM Appointment a
             WHERE a.businessEmployee.business.id = :businessId
             AND (:employeeId IS NULL OR a.businessEmployee.user.id = :employeeId)
             AND a.createdDate BETWEEN :from AND :to
             AND a.guest.id IN (
                 SELECT a2.guest.id
                 FROM Appointment a2
                 WHERE a2.businessEmployee.business.id = :businessId
                 AND (:employeeId IS NULL OR a2.businessEmployee.user.id = :employeeId)
                 AND a2.createdDate < :from
             )
        """
    )
    Long countReturningCustomers(Long businessId, Long employeeId, Instant from, Instant to);

    //most active guest
    @Query(
        """
             SELECT a.guest.name, COUNT(a.id)
             FROM Appointment a
             WHERE a.businessEmployee.business.id = :businessId
             AND (:employeeId IS NULL OR a.businessEmployee.user.id = :employeeId)
             AND a.createdDate BETWEEN :from AND :to
             GROUP BY a.guest.id, a.guest.name
             ORDER BY COUNT(a.id) DESC
        """
    )
    List<Object[]> findTopCustomer(
        @Param("businessId") Long businessId,
        @Param("employeeId") Long employeeId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        Pageable pageable
    );
}
