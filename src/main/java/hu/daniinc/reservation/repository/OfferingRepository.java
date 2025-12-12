package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Offering entity.
 */
@SuppressWarnings("unused")
@Repository
@Where(clause = "status <> 'DELETED'")
public interface OfferingRepository extends JpaRepository<Offering, Long>, JpaSpecificationExecutor<Offering> {
    @Query(
        "select o from Offering o where o.businessEmployee.user.login = ?#{authentication.name} and o.businessEmployee.business.id = :businessId"
    )
    Page<Offering> getAllByBusinessIdAndLoggedInEmployee(@Param("businessId") Long businessId, Pageable pageable);

    @Query("select o from Offering o where o.businessEmployee.business.id = ?1 and o.status = 'ACTIVE'")
    Page<Offering> findAllByBusinessId(Long id, Pageable pageable);

    @Query(
        "select o from Offering o where o.businessEmployee.user.login = ?#{authentication.name} and o.businessEmployee.business.id = :businessId"
    )
    List<Offering> getAllByLoggedInEmployee(@Param(value = "businessId") Long businessId);

    @Query("select o from Offering o where o.id = ?1 and o.businessEmployee.business.owner.login = ?#{authentication.name}")
    Optional<Offering> findByIdToBusiness(Long offeringId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END FROM Offering o WHERE o.id = ?1 and o.businessEmployee.business.id = ?2")
    Boolean isBusinessHasTheOffer(Long offeringId, Long businessId);

    @Query("select o from Offering o where o.businessEmployee.id = :businessEmployeeId")
    List<Offering> getAllByBusinessEmployee(@Param(value = "businessEmployeeId") Long businessEmployeeId);
    //    Page<Offering> findAllPublicOfferingsByBusinessId(Specification<Offering> spec, Pageable pageable);
}
