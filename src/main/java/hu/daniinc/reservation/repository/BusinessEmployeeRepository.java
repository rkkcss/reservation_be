package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessEmployeeRepository extends JpaRepository<BusinessEmployee, Long>, JpaSpecificationExecutor<BusinessEmployee> {
    @Query("select be from BusinessEmployee be where be.business.id = ?1 and be.user.login = ?#{authentication.name}")
    Optional<BusinessEmployee> findByUserLoginAndBusinessId(Long businessId);

    @Query("select be from BusinessEmployee be where be.user.login = ?#{authentication.name}")
    Set<BusinessEmployee> findAllByUserLogin();

    @Query(
        "select be from BusinessEmployee be " +
        "where be.business.id = :businessId " +
        "and be.business.owner.login = ?#{authentication.name}" +
        "and (LOWER(be.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
        "or LOWER(be.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Set<BusinessEmployee> searchByNameAndBusinessId(Long businessId, String search);

    @Query("select be from BusinessEmployee be where be.business.id = :businessId")
    Page<BusinessEmployee> findAllByBusinessId(Long businessId, Pageable pageable);

    //find business Employee by business Id and employee (user) id
    @Query("select be from BusinessEmployee be where be.business.id = :businessId and be.user.id = :employeeId")
    Optional<BusinessEmployee> findByBusinessIdAndEmployeeId(Long businessId, Long employeeId);

    @Query(
        "select be from BusinessEmployee be where be.business.id = :businessId " +
        "AND be.status <> 'DELETED' " +
        "AND be.status <> 'INACTIVE'"
    )
    List<BusinessEmployee> findAllPublicByBusinessId(Long businessId);
}
