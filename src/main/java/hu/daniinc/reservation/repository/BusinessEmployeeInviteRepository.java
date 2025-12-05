package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.BusinessEmployeeInvite;
import hu.daniinc.reservation.service.dto.BusinessEmployeeInviteDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessEmployeeInviteRepository extends CrudRepository<BusinessEmployeeInvite, Long> {
    @Query("select bei from BusinessEmployeeInvite bei where bei.token = ?1")
    Optional<BusinessEmployeeInvite> findByToken(String inviteToken);

    @Query(
        "SELECT CASE WHEN COUNT(bei) > 0 THEN true ELSE false END " +
        "FROM BusinessEmployeeInvite bei " +
        "WHERE bei.email = :email " +
        "AND bei.business.id = :businessId " +
        "AND bei.used = false"
    )
    boolean existsByEmailAndBusinessIdAndUsedFalse(@Param("email") String email, @Param("businessId") Long businessId);

    @Query("select bei from BusinessEmployeeInvite bei where bei.business.id = :businessId")
    Page<BusinessEmployeeInvite> findAllByPendingPagination(@Param(value = "businessId") Long businessId, Pageable pageable);

    @Query(
        "select bei from BusinessEmployeeInvite bei where bei.token = :token " +
        "AND bei.used = false " +
        "AND bei.expiresAt > current_timestamp"
    )
    Optional<BusinessEmployeeInvite> findOneByToken(@Param(value = "token") String token);
}
