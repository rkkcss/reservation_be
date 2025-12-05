package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Guest;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.EnsuresKeyFor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Guest entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    @Query(
        "SELECT g FROM Guest g " +
        "WHERE (LOWER(g.name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
        "OR LOWER(g.email) LIKE LOWER(CONCAT('%', ?1, '%')) " +
        "OR LOWER(g.phoneNumber) LIKE LOWER(CONCAT('%', ?1, '%'))) " +
        "AND g.business.owner.login = ?#{authentication.name} " +
        "ORDER BY g.name DESC LIMIT 10"
    )
    List<Guest> searchByName(String name);

    @Query("select g from Guest g where g.business.owner.login = ?#{authentication.name}")
    Page<Guest> findAllByLoggedInUser(Pageable pageable);

    @Query("select g from Guest g where LOWER(g.email) = LOWER(?1) and g.business.id = ?2")
    Optional<Guest> findByEmail(String email, Long businessId);
}
