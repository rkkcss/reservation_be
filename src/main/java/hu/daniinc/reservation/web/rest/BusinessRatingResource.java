package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.repository.BusinessRatingRepository;
import hu.daniinc.reservation.service.BusinessRatingService;
import hu.daniinc.reservation.service.dto.BusinessRatingDTO;
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
 * REST controller for managing {@link hu.daniinc.reservation.domain.BusinessRating}.
 */
@RestController
@RequestMapping("/api/business-ratings")
public class BusinessRatingResource {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessRatingResource.class);

    private static final String ENTITY_NAME = "businessRating";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BusinessRatingService businessRatingService;

    private final BusinessRatingRepository businessRatingRepository;

    public BusinessRatingResource(BusinessRatingService businessRatingService, BusinessRatingRepository businessRatingRepository) {
        this.businessRatingService = businessRatingService;
        this.businessRatingRepository = businessRatingRepository;
    }

    /**
     * {@code POST  /business-ratings} : Create a new businessRating.
     *
     * @param businessRatingDTO the businessRatingDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new businessRatingDTO, or with status {@code 400 (Bad Request)} if the businessRating has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BusinessRatingDTO> createBusinessRating(@Valid @RequestBody BusinessRatingDTO businessRatingDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save BusinessRating : {}", businessRatingDTO);
        if (businessRatingDTO.getId() != null) {
            throw new BadRequestAlertException("A new businessRating cannot already have an ID", ENTITY_NAME, "idexists");
        }
        businessRatingDTO = businessRatingService.save(businessRatingDTO);
        return ResponseEntity.created(new URI("/api/business-ratings/" + businessRatingDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, businessRatingDTO.getId().toString()))
            .body(businessRatingDTO);
    }

    /**
     * {@code PUT  /business-ratings/:id} : Updates an existing businessRating.
     *
     * @param id the id of the businessRatingDTO to save.
     * @param businessRatingDTO the businessRatingDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated businessRatingDTO,
     * or with status {@code 400 (Bad Request)} if the businessRatingDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the businessRatingDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BusinessRatingDTO> updateBusinessRating(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BusinessRatingDTO businessRatingDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BusinessRating : {}, {}", id, businessRatingDTO);
        if (businessRatingDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, businessRatingDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!businessRatingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        businessRatingDTO = businessRatingService.update(businessRatingDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, businessRatingDTO.getId().toString()))
            .body(businessRatingDTO);
    }

    /**
     * {@code PATCH  /business-ratings/:id} : Partial updates given fields of an existing businessRating, field will ignore if it is null
     *
     * @param id the id of the businessRatingDTO to save.
     * @param businessRatingDTO the businessRatingDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated businessRatingDTO,
     * or with status {@code 400 (Bad Request)} if the businessRatingDTO is not valid,
     * or with status {@code 404 (Not Found)} if the businessRatingDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the businessRatingDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BusinessRatingDTO> partialUpdateBusinessRating(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BusinessRatingDTO businessRatingDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BusinessRating partially : {}, {}", id, businessRatingDTO);
        if (businessRatingDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, businessRatingDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!businessRatingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BusinessRatingDTO> result = businessRatingService.partialUpdate(businessRatingDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, businessRatingDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /business-ratings} : get all the businessRatings.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of businessRatings in body.
     */
    @GetMapping("")
    public ResponseEntity<List<BusinessRatingDTO>> getAllBusinessRatings(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of BusinessRatings");
        Page<BusinessRatingDTO> page = businessRatingService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /business-ratings/:id} : get the "id" businessRating.
     *
     * @param id the id of the businessRatingDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the businessRatingDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusinessRatingDTO> getBusinessRating(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BusinessRating : {}", id);
        Optional<BusinessRatingDTO> businessRatingDTO = businessRatingService.findOne(id);
        return ResponseUtil.wrapOrNotFound(businessRatingDTO);
    }

    /**
     * {@code DELETE  /business-ratings/:id} : delete the "id" businessRating.
     *
     * @param id the id of the businessRatingDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusinessRating(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BusinessRating : {}", id);
        businessRatingService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
