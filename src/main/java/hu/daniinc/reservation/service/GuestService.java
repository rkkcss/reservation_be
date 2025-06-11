package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.GuestDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.Guest}.
 */
public interface GuestService {
    /**
     * Save a guest.
     *
     * @param guestDTO the entity to save.
     * @return the persisted entity.
     */
    GuestDTO save(GuestDTO guestDTO);

    /**
     * Updates a guest.
     *
     * @param guestDTO the entity to update.
     * @return the persisted entity.
     */
    GuestDTO update(GuestDTO guestDTO);

    /**
     * Partially updates a guest.
     *
     * @param guestDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<GuestDTO> partialUpdate(GuestDTO guestDTO);

    /**
     * Get all the guests.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<GuestDTO> findAll(Pageable pageable);

    /**
     * Get all the GuestDTO where Appointment is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<GuestDTO> findAllWhereAppointmentIsNull();

    /**
     * Get the "id" guest.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<GuestDTO> findOne(Long id);

    /**
     * Delete the "id" guest.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    List<GuestDTO> findAllBySearchString(String searchString);

    Page<GuestDTO> findAllByLoggedInUser(Pageable pageable);
}
