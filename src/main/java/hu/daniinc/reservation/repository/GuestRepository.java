package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Guest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Guest entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GuestRepository extends JpaRepository<Guest, Long>, JpaSpecificationExecutor<Guest> {
    @Query(
        "SELECT g FROM Guest g " +
        "WHERE (LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
        "OR LOWER(g.email) LIKE LOWER(CONCAT('%', :name, '%')) " +
        "OR LOWER(g.phoneNumber) LIKE LOWER(CONCAT('%', :name, '%'))) " +
        "AND g.businessEmployee.user.login = ?#{authentication.name} " +
        "AND g.businessEmployee.business.id = :businessId " +
        "ORDER BY g.name DESC LIMIT 10"
    )
    List<Guest> searchByName(@Param("businessId") Long businessId, @Param("name") String name);

    @Query("select g from Guest g where g.businessEmployee.user.login = ?#{authentication.name}")
    Page<Guest> findAllByLoggedInUser(Pageable pageable);

    @Query("select g from Guest g where LOWER(g.email) = LOWER(?1) and g.businessEmployee.business.id = ?2")
    Optional<Guest> findByEmailByBusinessId(String email, Long businessId);

    @Query("select g from Guest g where g.businessEmployee.business.id = :businessId and g.id = :guestId")
    Optional<Guest> findByGuestIdAndBusinessId(@Param("guestId") Long guestId, @Param("businessId") Long businessId);

    //check the business have the current guest or not
    @Query(
        "select count(a) > 0 from Appointment a " +
        "join a.businessEmployee be " +
        "join be.business b " +
        "where a.guest.id = :guestId " +
        "and b.id = :businessId " +
        "and be.user.login = ?#{authentication.name}"
    )
    boolean isGuestPartOfTheBusinessAndUserHasAccess(@Param("businessId") Long businessId, @Param("guestId") Long guestId);
}
