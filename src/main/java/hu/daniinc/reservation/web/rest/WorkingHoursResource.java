package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.repository.WorkingHoursRepository;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.service.WorkingHoursService;
import hu.daniinc.reservation.service.dto.WorkingHoursDTO;
import hu.daniinc.reservation.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
 * REST controller for managing {@link hu.daniinc.reservation.domain.WorkingHours}.
 */
@RestController
@RequestMapping("/api/working-hours")
public class WorkingHoursResource {

    private static final Logger LOG = LoggerFactory.getLogger(WorkingHoursResource.class);

    private static final String ENTITY_NAME = "workingHours";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final WorkingHoursService workingHoursService;

    private final WorkingHoursRepository workingHoursRepository;

    public WorkingHoursResource(WorkingHoursService workingHoursService, WorkingHoursRepository workingHoursRepository) {
        this.workingHoursService = workingHoursService;
        this.workingHoursRepository = workingHoursRepository;
    }

    /**
     * {@code POST  /working-hours} : Create a new workingHours.
     *
     * @param workingHoursDTO the workingHoursDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new workingHoursDTO, or with status {@code 400 (Bad Request)} if the workingHours has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<WorkingHoursDTO> createWorkingHours(@Valid @RequestBody WorkingHoursDTO workingHoursDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save WorkingHours : {}", workingHoursDTO);
        if (workingHoursDTO.getId() != null) {
            throw new BadRequestAlertException("A new workingHours cannot already have an ID", ENTITY_NAME, "idexists");
        }
        workingHoursDTO = workingHoursService.save(workingHoursDTO);
        return ResponseEntity.created(new URI("/api/working-hours/" + workingHoursDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, workingHoursDTO.getId().toString()))
            .body(workingHoursDTO);
    }

    /**
     * {@code PUT  /working-hours/:id} : Updates an existing workingHours.
     *
     * @param id the id of the workingHoursDTO to save.
     * @param workingHoursDTO the workingHoursDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated workingHoursDTO,
     * or with status {@code 400 (Bad Request)} if the workingHoursDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the workingHoursDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkingHoursDTO> updateWorkingHours(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody WorkingHoursDTO workingHoursDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update WorkingHours : {}, {}", id, workingHoursDTO);
        if (workingHoursDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, workingHoursDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!workingHoursRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        workingHoursDTO = workingHoursService.update(workingHoursDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, workingHoursDTO.getId().toString()))
            .body(workingHoursDTO);
    }

    /**
     * {@code PATCH  /working-hours/:id} : Partial updates given fields of an existing workingHours, field will ignore if it is null
     *
     * @param id the id of the workingHoursDTO to save.
     * @param workingHoursDTO the workingHoursDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated workingHoursDTO,
     * or with status {@code 400 (Bad Request)} if the workingHoursDTO is not valid,
     * or with status {@code 404 (Not Found)} if the workingHoursDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the workingHoursDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WorkingHoursDTO> partialUpdateWorkingHours(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody WorkingHoursDTO workingHoursDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update WorkingHours partially : {}, {}", id, workingHoursDTO);
        if (workingHoursDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, workingHoursDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!workingHoursRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<WorkingHoursDTO> result = workingHoursService.partialUpdate(workingHoursDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, workingHoursDTO.getId().toString())
        );
    }

    /**
     * {@code DELETE  /working-hours/:id} : delete the "id" workingHours.
     *
     * @param id the id of the workingHoursDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkingHours(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete WorkingHours : {}", id);
        workingHoursService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    //get all the working hours by business id and employeeId
    @GetMapping("/business/{businessId}/business-employee/{employeeId}")
    public ResponseEntity<List<WorkingHoursDTO>> getBusinessWorkingHours(
        @PathVariable("businessId") Long businessId,
        @PathVariable("employeeId") Long employeeId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(workingHoursService.getAllByBusinessAndEmployeeId(businessId, employeeId));
    }

    @GetMapping("/business/{businessId}/my")
    public ResponseEntity<List<WorkingHoursDTO>> getMyWorkingHoursByBusiness(@PathVariable("businessId") Long businessId) {
        LOG.debug("REST request to get My WorkingHours");
        return ResponseEntity.status(HttpStatus.OK).body(workingHoursService.getAllOwnWorkingHours(businessId));
    }

    @PutMapping("/business/{businessId}/business-employee/{employeeId}")
    @RequiredBusinessPermission(value = { BusinessPermission.EDIT_ALL_SCHEDULES, BusinessPermission.EDIT_OWN_SCHEDULE })
    public ResponseEntity<Void> updateOpeningHours(
        @PathVariable("businessId") Long businessId,
        @PathVariable("employeeId") Long employeeId,
        @RequestBody List<WorkingHoursDTO> newHours
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(workingHoursService.updateWorkingHours(businessId, employeeId, newHours));
    }
}
