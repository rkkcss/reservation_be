package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.BusinessOpeningHoursDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.BusinessOpeningHours}.
 */
public interface BusinessOpeningHoursService {
    /**
     * Save a businessOpeningHours.
     *
     * @param businessOpeningHoursDTO the entity to save.
     * @return the persisted entity.
     */
    BusinessOpeningHoursDTO save(BusinessOpeningHoursDTO businessOpeningHoursDTO);

    /**
     * Updates a businessOpeningHours.
     *
     * @param businessOpeningHoursDTO the entity to update.
     * @return the persisted entity.
     */
    BusinessOpeningHoursDTO update(BusinessOpeningHoursDTO businessOpeningHoursDTO);

    /**
     * Partially updates a businessOpeningHours.
     *
     * @param businessOpeningHoursDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BusinessOpeningHoursDTO> partialUpdate(BusinessOpeningHoursDTO businessOpeningHoursDTO);

    /**
     * Get all the businessOpeningHours.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<BusinessOpeningHoursDTO> findAll(Pageable pageable);

    /**
     * Get the "id" businessOpeningHours.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BusinessOpeningHoursDTO> findOne(Long id);

    /**
     * Delete the "id" businessOpeningHours.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
