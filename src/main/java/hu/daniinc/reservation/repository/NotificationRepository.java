package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Notification;
import hu.daniinc.reservation.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Query(
        """
        SELECT n FROM Notification n
        WHERE n.businessEmployee.id = :businessEmployeeId
        AND n.businessEmployee.user = :user
        AND (:unreadOnly IS NULL OR n.read = false)
        ORDER BY n.createdAt DESC
        """
    )
    Page<Notification> findAllByBusinessEmployee(
        @Param("businessEmployeeId") Long businessEmployeeId,
        @Param("user") User user,
        @Param("unreadOnly") Boolean unreadOnly,
        Pageable pageable
    );

    @Query(
        """
        SELECT n FROM Notification n
        WHERE n.id = :id
        AND n.businessEmployee.id = :businessEmployeeId
        AND n.businessEmployee.user = :user
        """
    )
    Optional<Notification> findByIdAndBusinessEmployeeAndUser(
        @Param("id") Long id,
        @Param("businessEmployeeId") Long businessEmployeeId,
        @Param("user") User user
    );

    @Query(
        """
        SELECT count(n) FROM Notification n
        WHERE n.businessEmployee.id = :businessEmployeeId
        AND n.businessEmployee.user = :user
        AND n.read = false
        """
    )
    long countUnreadByBusinessEmployeeAndUser(@Param("businessEmployeeId") Long businessEmployeeId, @Param("user") User user);

    @Query(
        """
        SELECT n.businessEmployee.id, COUNT(n)
        FROM Notification n
        WHERE n.businessEmployee.user = :user
        AND n.read = false
        GROUP BY n.businessEmployee.id
        """
    )
    List<Object[]> countUnreadGroupedByBusinessEmployee(@Param("user") User user);
}
