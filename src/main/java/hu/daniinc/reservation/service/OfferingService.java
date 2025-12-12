package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.OfferingDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.Offering}.
 */
public interface OfferingService {
    /**
     * Save a offering.
     *
     * @param offeringDTO the entity to save.
     * @return the persisted entity.
     */
    OfferingDTO createOffering(OfferingDTO offeringDTO, Long businessId, Long employeeId);

    /**
     * Updates a offering.
     *
     * @param offeringDTO the entity to update.
     * @return the persisted entity.
     */
    OfferingDTO update(OfferingDTO offeringDTO);

    /**
     * Partially updates a offering.
     *
     * @param offeringDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<OfferingDTO> partialUpdate(OfferingDTO offeringDTO, Long businessId);

    /**
     * Get all the offerings.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<OfferingDTO> findAll(Pageable pageable);

    /**
     * Get the "id" offering.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<OfferingDTO> findOne(Long id);

    /**
     * Delete the "id" offering.
     *
     * @param offerId the id of the entity.
     */
    void logicalDelete(Long offerId, Long businessId);

    Page<OfferingDTO> getAllByLoggedInEmployeeAndBusinessId(Long businessId, Pageable pageable);

    Page<OfferingDTO> getAllByBusinessId(Long id, Pageable pageable);

    List<OfferingDTO> getAllOfferingsByLoggedInEmployee(Long businessId);

    Page<OfferingDTO> getAllOfferingsByLoggedInBusinessId(Long businessId, Pageable pageable);

    List<OfferingDTO> getAllByBusinessEmployee(Long businessEmployeeId);

    Page<OfferingDTO> findAllPublicOfferingByBusinessId(Long businessId, String search, Pageable pageable);
}
