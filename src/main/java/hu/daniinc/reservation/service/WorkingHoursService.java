package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.WorkingHoursDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.WorkingHours}.
 */
public interface WorkingHoursService {
    /**
     * Save a workingHours.
     *
     * @param workingHoursDTO the entity to save.
     * @return the persisted entity.
     */
    WorkingHoursDTO save(WorkingHoursDTO workingHoursDTO);

    /**
     * Updates a workingHours.
     *
     * @param workingHoursDTO the entity to update.
     * @return the persisted entity.
     */
    WorkingHoursDTO update(WorkingHoursDTO workingHoursDTO);

    /**
     * Partially updates a workingHours.
     *
     * @param workingHoursDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<WorkingHoursDTO> partialUpdate(WorkingHoursDTO workingHoursDTO);

    /**
     * Get all the workingHours.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<WorkingHoursDTO> findAll(Pageable pageable);

    /**
     * Get the "id" workingHours.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<WorkingHoursDTO> findOne(Long id);

    /**
     * Delete the "id" workingHours.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    List<WorkingHoursDTO> getAllByBusinessAndEmployeeId(Long businessId, Long employeeId);

    Void updateWorkingHours(Long businessId, Long employeeId, List<WorkingHoursDTO> newHours);

    List<WorkingHoursDTO> getAllOwnWorkingHours(Long businessId);
}
