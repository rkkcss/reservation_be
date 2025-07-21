package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.Business}.
 */
public interface BusinessService {
    /**
     * Save a business.
     *
     * @param businessDTO the entity to save.
     * @return the persisted entity.
     */
    BusinessDTO save(BusinessDTO businessDTO);

    /**
     * Updates a business.
     *
     * @param businessDTO the entity to update.
     * @return the persisted entity.
     */
    BusinessDTO update(BusinessDTO businessDTO);

    /**
     * Partially updates a business.
     *
     * @param businessDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BusinessDTO> partialUpdate(BusinessDTO businessDTO);

    /**
     * Get all the businesses.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<BusinessDTO> findAll(Pageable pageable);

    /**
     * Get all the BusinessDTO where Appointment is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<BusinessDTO> findAllWhereAppointmentIsNull();

    /**
     * Get the "id" business.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BusinessDTO> findOne(Long id);

    /**
     * Delete the "id" business.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    BusinessDTO getBusinessByLoggedInUser();

    void changeBusinessLogo(String logo);

    void changeBusinessTheme(BusinessTheme theme);
}
