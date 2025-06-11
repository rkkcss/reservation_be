package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.repository.CustomWorkingHoursRepository;
import hu.daniinc.reservation.service.CustomWorkingHoursService;
import hu.daniinc.reservation.service.dto.CustomWorkingHoursDTO;
import hu.daniinc.reservation.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link hu.daniinc.reservation.domain.CustomWorkingHours}.
 */
@RestController
@RequestMapping("/api/custom-working-hours")
public class CustomWorkingHoursResource {

    private static final Logger LOG = LoggerFactory.getLogger(CustomWorkingHoursResource.class);

    private static final String ENTITY_NAME = "customWorkingHours";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CustomWorkingHoursService customWorkingHoursService;

    private final CustomWorkingHoursRepository customWorkingHoursRepository;

    public CustomWorkingHoursResource(
        CustomWorkingHoursService customWorkingHoursService,
        CustomWorkingHoursRepository customWorkingHoursRepository
    ) {
        this.customWorkingHoursService = customWorkingHoursService;
        this.customWorkingHoursRepository = customWorkingHoursRepository;
    }

    /**
     * {@code POST  /custom-working-hours} : Create a new customWorkingHours.
     *
     * @param customWorkingHoursDTO the customWorkingHoursDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new customWorkingHoursDTO, or with status {@code 400 (Bad Request)} if the customWorkingHours has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CustomWorkingHoursDTO> createCustomWorkingHours(@Valid @RequestBody CustomWorkingHoursDTO customWorkingHoursDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save CustomWorkingHours : {}", customWorkingHoursDTO);
        if (customWorkingHoursDTO.getId() != null) {
            throw new BadRequestAlertException("A new customWorkingHours cannot already have an ID", ENTITY_NAME, "idexists");
        }
        customWorkingHoursDTO = customWorkingHoursService.save(customWorkingHoursDTO);
        return ResponseEntity.created(new URI("/api/custom-working-hours/" + customWorkingHoursDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, customWorkingHoursDTO.getId().toString()))
            .body(customWorkingHoursDTO);
    }

    /**
     * {@code PUT  /custom-working-hours/:id} : Updates an existing customWorkingHours.
     *
     * @param id the id of the customWorkingHoursDTO to save.
     * @param customWorkingHoursDTO the customWorkingHoursDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated customWorkingHoursDTO,
     * or with status {@code 400 (Bad Request)} if the customWorkingHoursDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the customWorkingHoursDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomWorkingHoursDTO> updateCustomWorkingHours(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CustomWorkingHoursDTO customWorkingHoursDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CustomWorkingHours : {}, {}", id, customWorkingHoursDTO);
        if (customWorkingHoursDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, customWorkingHoursDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!customWorkingHoursRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        customWorkingHoursDTO = customWorkingHoursService.update(customWorkingHoursDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, customWorkingHoursDTO.getId().toString()))
            .body(customWorkingHoursDTO);
    }

    /**
     * {@code PATCH  /custom-working-hours/:id} : Partial updates given fields of an existing customWorkingHours, field will ignore if it is null
     *
     * @param id the id of the customWorkingHoursDTO to save.
     * @param customWorkingHoursDTO the customWorkingHoursDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated customWorkingHoursDTO,
     * or with status {@code 400 (Bad Request)} if the customWorkingHoursDTO is not valid,
     * or with status {@code 404 (Not Found)} if the customWorkingHoursDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the customWorkingHoursDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CustomWorkingHoursDTO> partialUpdateCustomWorkingHours(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CustomWorkingHoursDTO customWorkingHoursDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CustomWorkingHours partially : {}, {}", id, customWorkingHoursDTO);
        if (customWorkingHoursDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, customWorkingHoursDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!customWorkingHoursRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CustomWorkingHoursDTO> result = customWorkingHoursService.partialUpdate(customWorkingHoursDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, customWorkingHoursDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /custom-working-hours} : get all the customWorkingHours.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of customWorkingHours in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CustomWorkingHoursDTO>> getAllCustomWorkingHours(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of CustomWorkingHours");
        Page<CustomWorkingHoursDTO> page = customWorkingHoursService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /custom-working-hours/:id} : get the "id" customWorkingHours.
     *
     * @param id the id of the customWorkingHoursDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the customWorkingHoursDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomWorkingHoursDTO> getCustomWorkingHours(@PathVariable("id") Long id) {
        LOG.debug("REST request to get CustomWorkingHours : {}", id);
        Optional<CustomWorkingHoursDTO> customWorkingHoursDTO = customWorkingHoursService.findOne(id);
        return ResponseUtil.wrapOrNotFound(customWorkingHoursDTO);
    }

    /**
     * {@code DELETE  /custom-working-hours/:id} : delete the "id" customWorkingHours.
     *
     * @param id the id of the customWorkingHoursDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomWorkingHours(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete CustomWorkingHours : {}", id);
        customWorkingHoursService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
