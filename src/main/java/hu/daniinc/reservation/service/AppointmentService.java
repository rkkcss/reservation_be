package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.AppointmentDTO;
import hu.daniinc.reservation.service.dto.CreateAppointmentByGuestDTO;
import hu.daniinc.reservation.service.dto.CreateAppointmentRequestDTO;
import hu.daniinc.reservation.service.dto.UpdateAppointmentDTO;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Service Interface for managing {@link hu.daniinc.reservation.domain.Appointment}.
 */
public interface AppointmentService {
    /**
     * Save a appointment.
     *
     * @param appointmentDTO the entity to save.
     * @return the persisted entity.
     */
    AppointmentDTO save(AppointmentDTO appointmentDTO);

    /**
     * Updates a appointment.
     *
     * @param appointmentDTO the entity to update.
     * @return the persisted entity.
     */
    AppointmentDTO update(AppointmentDTO appointmentDTO);

    /**
     * Partially updates a appointment.
     *
     * @param updateAppointmentDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AppointmentDTO> partialUpdate(UpdateAppointmentDTO updateAppointmentDTO);

    /**
     * Get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    List<AppointmentDTO> findOverlappingAppointments(Instant startDate, Instant endDate, Long businessId, String name);

    /**
     * Get the "id" appointment.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AppointmentDTO> findOne(Long id);

    /**
     * Delete the "id" appointment.
     *
     * @param id the id of the entity.
     */
    void logicalDelete(Long id);

    Map<LocalDate, List<Instant>> getAvailableSlotsBetweenDates(
        Long businessId,
        Long employeeId,
        LocalDate from,
        LocalDate to,
        Duration slotLength
    );

    AppointmentDTO saveByOwner(CreateAppointmentRequestDTO createAppointmentRequestDTO);

    AppointmentDTO saveAppointmentByGuest(Long businessId, Long employeeId, CreateAppointmentByGuestDTO createAppointmentByGuestDTO);

    AppointmentDTO getAppointmentByModifierToken(String modifierToken);

    void cancelByModifierToken(String modifierToken);

    List<AppointmentDTO> getAllPendingAppointments(Long businessId);

    AppointmentDTO approveAppointment(Long appointmentId, Long employeeId);

    AppointmentDTO cancelAppointment(Long id, Long employeeId);
}
