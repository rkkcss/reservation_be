package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.BusinessRatingDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.BusinessRating}.
 */
public interface BusinessRatingService {
    /**
     * Save a businessRating.
     *
     * @param businessRatingDTO the entity to save.
     * @return the persisted entity.
     */
    BusinessRatingDTO save(BusinessRatingDTO businessRatingDTO);

    /**
     * Updates a businessRating.
     *
     * @param businessRatingDTO the entity to update.
     * @return the persisted entity.
     */
    BusinessRatingDTO update(BusinessRatingDTO businessRatingDTO);

    /**
     * Partially updates a businessRating.
     *
     * @param businessRatingDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BusinessRatingDTO> partialUpdate(BusinessRatingDTO businessRatingDTO);

    /**
     * Get all the businessRatings.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<BusinessRatingDTO> findAll(Pageable pageable);

    /**
     * Get the "id" businessRating.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BusinessRatingDTO> findOne(Long id);

    /**
     * Delete the "id" businessRating.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
