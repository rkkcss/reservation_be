package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.repository.OfferingRepository;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.service.OfferingService;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.GuestDTO;
import hu.daniinc.reservation.service.dto.OfferingDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link hu.daniinc.reservation.domain.Offering}.
 */
@RestController
@RequestMapping("/api/offerings")
public class OfferingResource {

    private static final Logger LOG = LoggerFactory.getLogger(OfferingResource.class);

    private static final String ENTITY_NAME = "offering";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OfferingService offeringService;

    private final OfferingRepository offeringRepository;

    public OfferingResource(OfferingService offeringService, OfferingRepository offeringRepository) {
        this.offeringService = offeringService;
        this.offeringRepository = offeringRepository;
    }

    /**
     * {@code POST  /offerings} : Create a new offering.
     *
     * @param offeringDTO the offeringDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new offeringDTO, or with status {@code 400 (Bad Request)} if the offering has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/business/{businessId}/business-employee/{employeeId}")
    @RequiredBusinessPermission(value = { BusinessPermission.EDIT_ALL_SERVICES, BusinessPermission.EDIT_OWN_SERVICES })
    public ResponseEntity<OfferingDTO> createOffering(
        @Valid @RequestBody OfferingDTO offeringDTO,
        @PathVariable Long businessId,
        @PathVariable Long employeeId
    ) throws URISyntaxException {
        LOG.debug("REST request to save Offering : {}", offeringDTO);
        if (offeringDTO.getId() != null) {
            throw new BadRequestAlertException("A new offering cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (businessId == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        offeringDTO = offeringService.createOffering(offeringDTO, businessId, employeeId);
        return ResponseEntity.created(new URI("/api/offerings/" + offeringDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, offeringDTO.getId().toString()))
            .body(offeringDTO);
    }

    /**
     * {@code PUT  /offerings/:id} : Updates an existing offering.
     *
     * @param id the id of the offeringDTO to save.
     * @param offeringDTO the offeringDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated offeringDTO,
     * or with status {@code 400 (Bad Request)} if the offeringDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the offeringDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OfferingDTO> updateOffering(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody OfferingDTO offeringDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Offering : {}, {}", id, offeringDTO);
        if (offeringDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, offeringDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!offeringRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        offeringDTO = offeringService.update(offeringDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, offeringDTO.getId().toString()))
            .body(offeringDTO);
    }

    /**
     * {@code PATCH  /offerings/:id} : Partial updates given fields of an existing offering, field will ignore if it is null
     *
     * @param id the id of the offeringDTO to save.
     * @param offeringDTO the offeringDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated offeringDTO,
     * or with status {@code 400 (Bad Request)} if the offeringDTO is not valid,
     * or with status {@code 404 (Not Found)} if the offeringDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the offeringDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{offeringId}/business/{businessId}", consumes = { "application/json", "application/merge-patch+json" })
    @RequiredBusinessPermission(value = { BusinessPermission.EDIT_OWN_SERVICES, BusinessPermission.EDIT_ALL_SERVICES })
    public ResponseEntity<OfferingDTO> partialUpdateOffering(
        @PathVariable(value = "offeringId") final Long offeringId,
        @PathVariable(value = "businessId") final Long businessId,
        @NotNull @RequestBody OfferingDTO offeringDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Offering partially : {}, {}, {}", offeringId, offeringDTO, businessId);
        if (offeringDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(offeringId, offeringDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!offeringRepository.existsById(offeringId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<OfferingDTO> result = offeringService.partialUpdate(offeringDTO, businessId);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, offeringDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /offerings/{businessId} : get all the offerings.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of offerings in body.
     */
    @GetMapping("/business/{businessId}/employee")
    public ResponseEntity<List<OfferingDTO>> getAllOwnOfferings(@PathVariable(value = "businessId") Long businessId) {
        LOG.debug("REST request to get own Offerings by business id : {}", businessId);
        List<OfferingDTO> result = offeringService.getAllOfferingsByLoggedInEmployee(businessId);
        return ResponseEntity.ok().body(result);
    }

    // return all the offerings by businessId
    @GetMapping("/business/{businessId}/")
    public ResponseEntity<List<OfferingDTO>> getAllByBusiness(@PathVariable(value = "businessId") Long businessId, Pageable pageable) {
        LOG.debug("REST request to get Offerings by business id : {}", businessId);
        Page<OfferingDTO> page = offeringService.getAllOfferingsByLoggedInBusinessId(businessId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    // return all offerings by business employee
    @GetMapping("/business-employee/{businessEmployeeId}")
    public ResponseEntity<List<OfferingDTO>> getAllByBusinessEmployee(@PathVariable(value = "businessEmployeeId") Long businessEmployeeId) {
        LOG.debug("REST request to get Offerings by business employee id : {}", businessEmployeeId);
        return ResponseEntity.ok().body(offeringService.getAllByBusinessEmployee(businessEmployeeId));
    }

    //return all the business related offerings - PUBLIC
    @GetMapping("/public/business/{businessId}")
    public ResponseEntity<List<OfferingDTO>> getAllPublicByBusinessId(
        @PathVariable(value = "businessId") Long businessId,
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        Page<OfferingDTO> page = offeringService.findAllPublicOfferingByBusinessId(businessId, search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /offerings/:id} : get the "id" offering.
     *
     * @param id the id of the offeringDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the offeringDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OfferingDTO> getOffering(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Offering : {}", id);
        Optional<OfferingDTO> offeringDTO = offeringService.findOne(id);
        return ResponseUtil.wrapOrNotFound(offeringDTO);
    }

    /**
     * {@code DELETE  /offerings/:id} : delete the "id" offering.
     *
     * @param offerId the id of the offeringDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PatchMapping("/{offerId}/business/{businessId}")
    @RequiredBusinessPermission(value = { BusinessPermission.EDIT_ALL_SERVICES, BusinessPermission.EDIT_OWN_SERVICES })
    public ResponseEntity<Void> deleteOffering(@PathVariable("offerId") Long offerId, @PathVariable("businessId") Long businessId) {
        LOG.debug("REST request to delete Offering : {}", offerId);
        offeringService.logicalDelete(offerId, businessId);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, offerId.toString()))
            .build();
    }

    //get all the offerings by logged in user / owner
    @GetMapping("/business/{businessId}/my")
    @RequiredBusinessPermission(value = BusinessPermission.VIEW_SERVICES)
    public ResponseEntity<List<OfferingDTO>> getAllByLoggedInEmployee(
        @PathVariable(value = "businessId") Long businessId,
        Pageable pageable
    ) {
        if (businessId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LOG.debug("REST request to get a page of Offerings by logged in business employee and business id : {}", businessId);
        Page<OfferingDTO> page = offeringService.getAllByLoggedInEmployeeAndBusinessId(businessId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/business/{id}")
    public ResponseEntity<List<OfferingDTO>> getAllByBusinessId(@PathVariable(value = "id") Long id, Pageable pageable) {
        Page<OfferingDTO> page = offeringService.getAllByBusinessId(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        headers.add("X-Page-Size", String.valueOf(pageable.getPageSize()));

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
