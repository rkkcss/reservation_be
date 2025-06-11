package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.repository.BusinessOpeningHoursRepository;
import hu.daniinc.reservation.service.BusinessOpeningHoursService;
import hu.daniinc.reservation.service.dto.BusinessOpeningHoursDTO;
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
 * REST controller for managing {@link hu.daniinc.reservation.domain.BusinessOpeningHours}.
 */
@RestController
@RequestMapping("/api/business-opening-hours")
public class BusinessOpeningHoursResource {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessOpeningHoursResource.class);

    private static final String ENTITY_NAME = "businessOpeningHours";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BusinessOpeningHoursService businessOpeningHoursService;

    private final BusinessOpeningHoursRepository businessOpeningHoursRepository;

    public BusinessOpeningHoursResource(
        BusinessOpeningHoursService businessOpeningHoursService,
        BusinessOpeningHoursRepository businessOpeningHoursRepository
    ) {
        this.businessOpeningHoursService = businessOpeningHoursService;
        this.businessOpeningHoursRepository = businessOpeningHoursRepository;
    }

    /**
     * {@code POST  /business-opening-hours} : Create a new businessOpeningHours.
     *
     * @param businessOpeningHoursDTO the businessOpeningHoursDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new businessOpeningHoursDTO, or with status {@code 400 (Bad Request)} if the businessOpeningHours has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BusinessOpeningHoursDTO> createBusinessOpeningHours(
        @Valid @RequestBody BusinessOpeningHoursDTO businessOpeningHoursDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save BusinessOpeningHours : {}", businessOpeningHoursDTO);
        if (businessOpeningHoursDTO.getId() != null) {
            throw new BadRequestAlertException("A new businessOpeningHours cannot already have an ID", ENTITY_NAME, "idexists");
        }
        businessOpeningHoursDTO = businessOpeningHoursService.save(businessOpeningHoursDTO);
        return ResponseEntity.created(new URI("/api/business-opening-hours/" + businessOpeningHoursDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, businessOpeningHoursDTO.getId().toString()))
            .body(businessOpeningHoursDTO);
    }

    /**
     * {@code PUT  /business-opening-hours/:id} : Updates an existing businessOpeningHours.
     *
     * @param id the id of the businessOpeningHoursDTO to save.
     * @param businessOpeningHoursDTO the businessOpeningHoursDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated businessOpeningHoursDTO,
     * or with status {@code 400 (Bad Request)} if the businessOpeningHoursDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the businessOpeningHoursDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BusinessOpeningHoursDTO> updateBusinessOpeningHours(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BusinessOpeningHoursDTO businessOpeningHoursDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BusinessOpeningHours : {}, {}", id, businessOpeningHoursDTO);
        if (businessOpeningHoursDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, businessOpeningHoursDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!businessOpeningHoursRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        businessOpeningHoursDTO = businessOpeningHoursService.update(businessOpeningHoursDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, businessOpeningHoursDTO.getId().toString()))
            .body(businessOpeningHoursDTO);
    }

    /**
     * {@code PATCH  /business-opening-hours/:id} : Partial updates given fields of an existing businessOpeningHours, field will ignore if it is null
     *
     * @param id the id of the businessOpeningHoursDTO to save.
     * @param businessOpeningHoursDTO the businessOpeningHoursDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated businessOpeningHoursDTO,
     * or with status {@code 400 (Bad Request)} if the businessOpeningHoursDTO is not valid,
     * or with status {@code 404 (Not Found)} if the businessOpeningHoursDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the businessOpeningHoursDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BusinessOpeningHoursDTO> partialUpdateBusinessOpeningHours(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BusinessOpeningHoursDTO businessOpeningHoursDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BusinessOpeningHours partially : {}, {}", id, businessOpeningHoursDTO);
        if (businessOpeningHoursDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, businessOpeningHoursDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!businessOpeningHoursRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BusinessOpeningHoursDTO> result = businessOpeningHoursService.partialUpdate(businessOpeningHoursDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, businessOpeningHoursDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /business-opening-hours} : get all the businessOpeningHours.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of businessOpeningHours in body.
     */
    @GetMapping("")
    public ResponseEntity<List<BusinessOpeningHoursDTO>> getAllBusinessOpeningHours(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of BusinessOpeningHours");
        Page<BusinessOpeningHoursDTO> page = businessOpeningHoursService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /business-opening-hours/:id} : get the "id" businessOpeningHours.
     *
     * @param id the id of the businessOpeningHoursDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the businessOpeningHoursDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusinessOpeningHoursDTO> getBusinessOpeningHours(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BusinessOpeningHours : {}", id);
        Optional<BusinessOpeningHoursDTO> businessOpeningHoursDTO = businessOpeningHoursService.findOne(id);
        return ResponseUtil.wrapOrNotFound(businessOpeningHoursDTO);
    }

    /**
     * {@code DELETE  /business-opening-hours/:id} : delete the "id" businessOpeningHours.
     *
     * @param id the id of the businessOpeningHoursDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusinessOpeningHours(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BusinessOpeningHours : {}", id);
        businessOpeningHoursService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
