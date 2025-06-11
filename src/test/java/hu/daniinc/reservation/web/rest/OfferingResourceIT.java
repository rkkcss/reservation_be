package hu.daniinc.reservation.web.rest;

import static hu.daniinc.reservation.domain.OfferingAsserts.*;
import static hu.daniinc.reservation.web.rest.TestUtil.createUpdateProxyForBean;
import static hu.daniinc.reservation.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.IntegrationTest;
import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.repository.OfferingRepository;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import hu.daniinc.reservation.service.mapper.OfferingMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link OfferingResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class OfferingResourceIT {

    private static final Integer DEFAULT_DURATION_MINUTES = 1;
    private static final Integer UPDATED_DURATION_MINUTES = 2;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/offerings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private OfferingMapper offeringMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOfferingMockMvc;

    private Offering offering;

    private Offering insertedOffering;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Offering createEntity() {
        return new Offering()
            .durationMinutes(DEFAULT_DURATION_MINUTES)
            .price(DEFAULT_PRICE)
            .description(DEFAULT_DESCRIPTION)
            .title(DEFAULT_TITLE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Offering createUpdatedEntity() {
        return new Offering()
            .durationMinutes(UPDATED_DURATION_MINUTES)
            .price(UPDATED_PRICE)
            .description(UPDATED_DESCRIPTION)
            .title(UPDATED_TITLE);
    }

    @BeforeEach
    public void initTest() {
        offering = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedOffering != null) {
            offeringRepository.delete(insertedOffering);
            insertedOffering = null;
        }
    }

    @Test
    @Transactional
    void createOffering() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Offering
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);
        var returnedOfferingDTO = om.readValue(
            restOfferingMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offeringDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            OfferingDTO.class
        );

        // Validate the Offering in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedOffering = offeringMapper.toEntity(returnedOfferingDTO);
        assertOfferingUpdatableFieldsEquals(returnedOffering, getPersistedOffering(returnedOffering));

        insertedOffering = returnedOffering;
    }

    @Test
    @Transactional
    void createOfferingWithExistingId() throws Exception {
        // Create the Offering with an existing ID
        offering.setId(1L);
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOfferingMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offeringDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDurationMinutesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offering.setDurationMinutes(null);

        // Create the Offering, which fails.
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        restOfferingMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offeringDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offering.setPrice(null);

        // Create the Offering, which fails.
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        restOfferingMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offeringDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offering.setTitle(null);

        // Create the Offering, which fails.
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        restOfferingMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offeringDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllOfferings() throws Exception {
        // Initialize the database
        insertedOffering = offeringRepository.saveAndFlush(offering);

        // Get all the offeringList
        restOfferingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(offering.getId().intValue())))
            .andExpect(jsonPath("$.[*].durationMinutes").value(hasItem(DEFAULT_DURATION_MINUTES)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)));
    }

    @Test
    @Transactional
    void getOffering() throws Exception {
        // Initialize the database
        insertedOffering = offeringRepository.saveAndFlush(offering);

        // Get the offering
        restOfferingMockMvc
            .perform(get(ENTITY_API_URL_ID, offering.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(offering.getId().intValue()))
            .andExpect(jsonPath("$.durationMinutes").value(DEFAULT_DURATION_MINUTES))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE));
    }

    @Test
    @Transactional
    void getNonExistingOffering() throws Exception {
        // Get the offering
        restOfferingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingOffering() throws Exception {
        // Initialize the database
        insertedOffering = offeringRepository.saveAndFlush(offering);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the offering
        Offering updatedOffering = offeringRepository.findById(offering.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedOffering are not directly saved in db
        em.detach(updatedOffering);
        updatedOffering
            .durationMinutes(UPDATED_DURATION_MINUTES)
            .price(UPDATED_PRICE)
            .description(UPDATED_DESCRIPTION)
            .title(UPDATED_TITLE);
        OfferingDTO offeringDTO = offeringMapper.toDto(updatedOffering);

        restOfferingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, offeringDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(offeringDTO))
            )
            .andExpect(status().isOk());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOfferingToMatchAllProperties(updatedOffering);
    }

    @Test
    @Transactional
    void putNonExistingOffering() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offering.setId(longCount.incrementAndGet());

        // Create the Offering
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOfferingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, offeringDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(offeringDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOffering() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offering.setId(longCount.incrementAndGet());

        // Create the Offering
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(offeringDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOffering() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offering.setId(longCount.incrementAndGet());

        // Create the Offering
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferingMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offeringDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOfferingWithPatch() throws Exception {
        // Initialize the database
        insertedOffering = offeringRepository.saveAndFlush(offering);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the offering using partial update
        Offering partialUpdatedOffering = new Offering();
        partialUpdatedOffering.setId(offering.getId());

        restOfferingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOffering.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOffering))
            )
            .andExpect(status().isOk());

        // Validate the Offering in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOfferingUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedOffering, offering), getPersistedOffering(offering));
    }

    @Test
    @Transactional
    void fullUpdateOfferingWithPatch() throws Exception {
        // Initialize the database
        insertedOffering = offeringRepository.saveAndFlush(offering);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the offering using partial update
        Offering partialUpdatedOffering = new Offering();
        partialUpdatedOffering.setId(offering.getId());

        partialUpdatedOffering
            .durationMinutes(UPDATED_DURATION_MINUTES)
            .price(UPDATED_PRICE)
            .description(UPDATED_DESCRIPTION)
            .title(UPDATED_TITLE);

        restOfferingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOffering.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOffering))
            )
            .andExpect(status().isOk());

        // Validate the Offering in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOfferingUpdatableFieldsEquals(partialUpdatedOffering, getPersistedOffering(partialUpdatedOffering));
    }

    @Test
    @Transactional
    void patchNonExistingOffering() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offering.setId(longCount.incrementAndGet());

        // Create the Offering
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOfferingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, offeringDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(offeringDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOffering() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offering.setId(longCount.incrementAndGet());

        // Create the Offering
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(offeringDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOffering() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offering.setId(longCount.incrementAndGet());

        // Create the Offering
        OfferingDTO offeringDTO = offeringMapper.toDto(offering);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferingMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(offeringDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Offering in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOffering() throws Exception {
        // Initialize the database
        insertedOffering = offeringRepository.saveAndFlush(offering);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the offering
        restOfferingMockMvc
            .perform(delete(ENTITY_API_URL_ID, offering.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return offeringRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Offering getPersistedOffering(Offering offering) {
        return offeringRepository.findById(offering.getId()).orElseThrow();
    }

    protected void assertPersistedOfferingToMatchAllProperties(Offering expectedOffering) {
        assertOfferingAllPropertiesEquals(expectedOffering, getPersistedOffering(expectedOffering));
    }

    protected void assertPersistedOfferingToMatchUpdatableProperties(Offering expectedOffering) {
        assertOfferingAllUpdatablePropertiesEquals(expectedOffering, getPersistedOffering(expectedOffering));
    }
}
