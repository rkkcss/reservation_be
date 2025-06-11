package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.CustomWorkingHoursDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.CustomWorkingHours}.
 */
public interface CustomWorkingHoursService {
    /**
     * Save a customWorkingHours.
     *
     * @param customWorkingHoursDTO the entity to save.
     * @return the persisted entity.
     */
    CustomWorkingHoursDTO save(CustomWorkingHoursDTO customWorkingHoursDTO);

    /**
     * Updates a customWorkingHours.
     *
     * @param customWorkingHoursDTO the entity to update.
     * @return the persisted entity.
     */
    CustomWorkingHoursDTO update(CustomWorkingHoursDTO customWorkingHoursDTO);

    /**
     * Partially updates a customWorkingHours.
     *
     * @param customWorkingHoursDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CustomWorkingHoursDTO> partialUpdate(CustomWorkingHoursDTO customWorkingHoursDTO);

    /**
     * Get all the customWorkingHours.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CustomWorkingHoursDTO> findAll(Pageable pageable);

    /**
     * Get the "id" customWorkingHours.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CustomWorkingHoursDTO> findOne(Long id);

    /**
     * Delete the "id" customWorkingHours.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
