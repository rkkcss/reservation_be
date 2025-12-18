package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.repository.AppointmentRepository;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.service.AppointmentService;
import hu.daniinc.reservation.service.dto.*;
import hu.daniinc.reservation.web.rest.errors.BadRequestAlertException;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link hu.daniinc.reservation.domain.Appointment}.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentResource {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentResource.class);

    private static final String ENTITY_NAME = "appointment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppointmentService appointmentService;

    private final AppointmentRepository appointmentRepository;

    public AppointmentResource(AppointmentService appointmentService, AppointmentRepository appointmentRepository) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * {@code POST  /appointments} : Create a new appointment.
     *
     * @param createAppointmentByGuestDTO the appointmentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appointmentDTO, or with status {@code 400 (Bad Request)} if the appointment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/business/{businessId}/business-employee/{employeeId}")
    public ResponseEntity<AppointmentDTO> createAppointment(
        @PathVariable("businessId") Long businessId,
        @PathVariable("employeeId") Long employeeId,
        @Valid @RequestBody CreateAppointmentByGuestDTO createAppointmentByGuestDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save Appointment : {}", createAppointmentByGuestDTO);
        if (businessId == null || businessId == 0 || employeeId == null || employeeId == 0) {
            throw new BadRequestAlertException("Invalid input", ENTITY_NAME, "idnull");
        }

        AppointmentDTO appointmentDTO = appointmentService.saveAppointmentByGuest(businessId, employeeId, createAppointmentByGuestDTO);
        return ResponseEntity.created(new URI("/api/appointments/" + appointmentDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, appointmentDTO.getId().toString()))
            .body(appointmentDTO);
    }

    @PostMapping("/create-by-owner")
    public ResponseEntity<AppointmentDTO> createAppointmentByOwner(
        @Valid @RequestBody CreateAppointmentRequestDTO createAppointmentRequestDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save Appointment by owner : {}", createAppointmentRequestDTO);
        AppointmentDTO appointmentDTO = appointmentService.saveByOwner(createAppointmentRequestDTO);
        return ResponseEntity.created(new URI("/api/appointments/" + appointmentDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, appointmentDTO.getId().toString()))
            .body(appointmentDTO);
    }

    /**
     * {@code PUT  /appointments/:id} : Updates an existing appointment.
     *
     * @param id the id of the appointmentDTO to save.
     * @param appointmentDTO the appointmentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointmentDTO,
     * or with status {@code 400 (Bad Request)} if the appointmentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appointmentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AppointmentDTO appointmentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Appointment : {}, {}", id, appointmentDTO);
        if (appointmentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appointmentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appointmentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        appointmentDTO = appointmentService.update(appointmentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, appointmentDTO.getId().toString()))
            .body(appointmentDTO);
    }

    /**
     * {@code PATCH  /appointments/:id} : Partial updates given fields of an existing appointment, field will ignore if it is null
     *
     * @param id the id of the appointmentDTO to save.
     * @param updateAppointmentDTO the appointmentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointmentDTO,
     * or with status {@code 400 (Bad Request)} if the appointmentDTO is not valid,
     * or with status {@code 404 (Not Found)} if the appointmentDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the appointmentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AppointmentDTO> partialUpdateAppointment(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody UpdateAppointmentDTO updateAppointmentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Appointment partially : {}, {}", id, updateAppointmentDTO);
        if (updateAppointmentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, updateAppointmentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appointmentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AppointmentDTO> result = appointmentService.partialUpdate(updateAppointmentDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, updateAppointmentDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /appointments} : get all the appointments by business.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appointments in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(
        @RequestParam Instant startDate,
        @RequestParam Instant endDate,
        @RequestParam Long businessId,
        @RequestParam String employeeName
    ) {
        LOG.debug("REST request to get a page of Appointments");
        List<AppointmentDTO> result = appointmentService.findOverlappingAppointments(startDate, endDate, businessId, employeeName);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code GET  /appointments/:id} : get the "id" appointment.
     *
     * @param id the id of the appointmentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appointmentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointment(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Appointment : {}", id);
        Optional<AppointmentDTO> appointmentDTO = appointmentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appointmentDTO);
    }

    /**
     * {@code POST  /appointments/:id} : logical delete the "id" appointment.
     *
     * @param id the id of the appointmentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PostMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Appointment : {}", id);
        appointmentService.logicalDelete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/businesses/{businessId}/employees/{employeeId}/available-slots")
    public Map<LocalDate, List<String>> getAvailableSlots(
        @PathVariable Long businessId,
        @PathVariable Long employeeId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long durationMinutes
    ) {
        Duration slotDuration = (durationMinutes != null) ? Duration.ofMinutes(durationMinutes) : Duration.ofMinutes(30);

        Map<LocalDate, List<Instant>> slotsMap = appointmentService.getAvailableSlotsBetweenDates(
            businessId,
            employeeId,
            startDate,
            endDate,
            slotDuration
        );

        // JSON-serialization
        return slotsMap
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Instant::toString).collect(Collectors.toList())));
    }

    @GetMapping("/cancel/{modifierToken}")
    public ResponseEntity<AppointmentDTO> getAppointmentByGuestnameAndAppointmentId(
        @PathVariable(value = "modifierToken") String modifierToken
    ) {
        LOG.debug("REST request to get Appointment by modifier token: {}", modifierToken);
        return ResponseEntity.status(HttpStatus.OK).body(appointmentService.getAppointmentByModifierToken(modifierToken));
    }

    @PostMapping("/cancel/{modifierToken}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable(value = "modifierToken") String modifierToken) {
        LOG.debug("REST request to cancel Appointment : {}", modifierToken);
        appointmentService.cancelByModifierToken(modifierToken);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, modifierToken))
            .build();
    }

    @GetMapping("/business/{businessId}/pendings")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments(@PathVariable(value = "businessId") Long businessId) {
        LOG.debug("REST request to get all pending appointments");
        return ResponseEntity.ok(appointmentService.getAllPendingAppointments(businessId));
    }

    @PatchMapping("/{id}/business-employee/{employeeId}/approve")
    @RequiredBusinessPermission(value = BusinessPermission.EDIT_OWN_BOOKINGS, businessIdParam = "employeeId")
    public ResponseEntity<AppointmentDTO> approveAppointment(
        @PathVariable(value = "id") Long id,
        @PathVariable(value = "employeeId") Long employeeId
    ) {
        LOG.debug("REST request to approve Appointment : {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(appointmentService.approveAppointment(id, employeeId));
    }

    @PatchMapping("/{id}/business-employee/{employeeId}/cancel")
    @RequiredBusinessPermission(value = BusinessPermission.EDIT_OWN_BOOKINGS, businessIdParam = "employeeId")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
        @PathVariable(value = "id") Long id,
        @PathVariable(value = "employeeId") Long employeeId
    ) {
        LOG.debug("REST request to cancel Appointment by owner : {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(appointmentService.cancelAppointment(id, employeeId));
    }
}
