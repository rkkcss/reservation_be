package hu.daniinc.reservation.repository;

import hu.daniinc.reservation.domain.GalleryImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {
    @Query("select gi from GalleryImage gi where gi.businessEmployee.id = :id")
    Page<GalleryImage> findAllByBusinessEmployeeId(@Param("id") Long id, Pageable pageable);

    @Query(
        "SELECT CASE WHEN COUNT(gi) > 0 THEN TRUE ELSE FALSE END FROM GalleryImage gi where gi.businessEmployee.id = :employeeId and gi.id = :imageId"
    )
    boolean isEmployeeHasImageById(@Param("employeeId") Long employeeId, @Param("imageId") Long imageId);
}
