package hu.daniinc.reservation.web.rest;

import static hu.daniinc.reservation.domain.BusinessRatingAsserts.*;
import static hu.daniinc.reservation.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.IntegrationTest;
import hu.daniinc.reservation.domain.BusinessRating;
import hu.daniinc.reservation.repository.BusinessRatingRepository;
import hu.daniinc.reservation.service.dto.BusinessRatingDTO;
import hu.daniinc.reservation.service.mapper.BusinessRatingMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link BusinessRatingResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BusinessRatingResourceIT {

    private static final Integer DEFAULT_NUMBER = 1;
    private static final Integer UPDATED_NUMBER = 2;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_IMAGE_URL = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/business-ratings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BusinessRatingRepository businessRatingRepository;

    @Autowired
    private BusinessRatingMapper businessRatingMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBusinessRatingMockMvc;

    private BusinessRating businessRating;

    private BusinessRating insertedBusinessRating;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BusinessRating createEntity() {
        return new BusinessRating().number(DEFAULT_NUMBER).description(DEFAULT_DESCRIPTION).imageUrl(DEFAULT_IMAGE_URL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BusinessRating createUpdatedEntity() {
        return new BusinessRating().number(UPDATED_NUMBER).description(UPDATED_DESCRIPTION).imageUrl(UPDATED_IMAGE_URL);
    }

    @BeforeEach
    public void initTest() {
        businessRating = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedBusinessRating != null) {
            businessRatingRepository.delete(insertedBusinessRating);
            insertedBusinessRating = null;
        }
    }

    @Test
    @Transactional
    void createBusinessRating() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BusinessRating
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);
        var returnedBusinessRatingDTO = om.readValue(
            restBusinessRatingMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(businessRatingDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BusinessRatingDTO.class
        );

        // Validate the BusinessRating in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBusinessRating = businessRatingMapper.toEntity(returnedBusinessRatingDTO);
        assertBusinessRatingUpdatableFieldsEquals(returnedBusinessRating, getPersistedBusinessRating(returnedBusinessRating));

        insertedBusinessRating = returnedBusinessRating;
    }

    @Test
    @Transactional
    void createBusinessRatingWithExistingId() throws Exception {
        // Create the BusinessRating with an existing ID
        businessRating.setId(1L);
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBusinessRatingMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        businessRating.setNumber(null);

        // Create the BusinessRating, which fails.
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        restBusinessRatingMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBusinessRatings() throws Exception {
        // Initialize the database
        insertedBusinessRating = businessRatingRepository.saveAndFlush(businessRating);

        // Get all the businessRatingList
        restBusinessRatingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(businessRating.getId().intValue())))
            .andExpect(jsonPath("$.[*].number").value(hasItem(DEFAULT_NUMBER)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGE_URL)));
    }

    @Test
    @Transactional
    void getBusinessRating() throws Exception {
        // Initialize the database
        insertedBusinessRating = businessRatingRepository.saveAndFlush(businessRating);

        // Get the businessRating
        restBusinessRatingMockMvc
            .perform(get(ENTITY_API_URL_ID, businessRating.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(businessRating.getId().intValue()))
            .andExpect(jsonPath("$.number").value(DEFAULT_NUMBER))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGE_URL));
    }

    @Test
    @Transactional
    void getNonExistingBusinessRating() throws Exception {
        // Get the businessRating
        restBusinessRatingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBusinessRating() throws Exception {
        // Initialize the database
        insertedBusinessRating = businessRatingRepository.saveAndFlush(businessRating);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the businessRating
        BusinessRating updatedBusinessRating = businessRatingRepository.findById(businessRating.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBusinessRating are not directly saved in db
        em.detach(updatedBusinessRating);
        updatedBusinessRating.number(UPDATED_NUMBER).description(UPDATED_DESCRIPTION).imageUrl(UPDATED_IMAGE_URL);
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(updatedBusinessRating);

        restBusinessRatingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, businessRatingDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isOk());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBusinessRatingToMatchAllProperties(updatedBusinessRating);
    }

    @Test
    @Transactional
    void putNonExistingBusinessRating() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessRating.setId(longCount.incrementAndGet());

        // Create the BusinessRating
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBusinessRatingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, businessRatingDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBusinessRating() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessRating.setId(longCount.incrementAndGet());

        // Create the BusinessRating
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessRatingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBusinessRating() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessRating.setId(longCount.incrementAndGet());

        // Create the BusinessRating
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessRatingMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBusinessRatingWithPatch() throws Exception {
        // Initialize the database
        insertedBusinessRating = businessRatingRepository.saveAndFlush(businessRating);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the businessRating using partial update
        BusinessRating partialUpdatedBusinessRating = new BusinessRating();
        partialUpdatedBusinessRating.setId(businessRating.getId());

        partialUpdatedBusinessRating.description(UPDATED_DESCRIPTION).imageUrl(UPDATED_IMAGE_URL);

        restBusinessRatingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBusinessRating.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBusinessRating))
            )
            .andExpect(status().isOk());

        // Validate the BusinessRating in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBusinessRatingUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBusinessRating, businessRating),
            getPersistedBusinessRating(businessRating)
        );
    }

    @Test
    @Transactional
    void fullUpdateBusinessRatingWithPatch() throws Exception {
        // Initialize the database
        insertedBusinessRating = businessRatingRepository.saveAndFlush(businessRating);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the businessRating using partial update
        BusinessRating partialUpdatedBusinessRating = new BusinessRating();
        partialUpdatedBusinessRating.setId(businessRating.getId());

        partialUpdatedBusinessRating.number(UPDATED_NUMBER).description(UPDATED_DESCRIPTION).imageUrl(UPDATED_IMAGE_URL);

        restBusinessRatingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBusinessRating.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBusinessRating))
            )
            .andExpect(status().isOk());

        // Validate the BusinessRating in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBusinessRatingUpdatableFieldsEquals(partialUpdatedBusinessRating, getPersistedBusinessRating(partialUpdatedBusinessRating));
    }

    @Test
    @Transactional
    void patchNonExistingBusinessRating() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessRating.setId(longCount.incrementAndGet());

        // Create the BusinessRating
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBusinessRatingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, businessRatingDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBusinessRating() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessRating.setId(longCount.incrementAndGet());

        // Create the BusinessRating
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessRatingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBusinessRating() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessRating.setId(longCount.incrementAndGet());

        // Create the BusinessRating
        BusinessRatingDTO businessRatingDTO = businessRatingMapper.toDto(businessRating);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessRatingMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessRatingDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the BusinessRating in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBusinessRating() throws Exception {
        // Initialize the database
        insertedBusinessRating = businessRatingRepository.saveAndFlush(businessRating);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the businessRating
        restBusinessRatingMockMvc
            .perform(delete(ENTITY_API_URL_ID, businessRating.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return businessRatingRepository.count();
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

    protected BusinessRating getPersistedBusinessRating(BusinessRating businessRating) {
        return businessRatingRepository.findById(businessRating.getId()).orElseThrow();
    }

    protected void assertPersistedBusinessRatingToMatchAllProperties(BusinessRating expectedBusinessRating) {
        assertBusinessRatingAllPropertiesEquals(expectedBusinessRating, getPersistedBusinessRating(expectedBusinessRating));
    }

    protected void assertPersistedBusinessRatingToMatchUpdatableProperties(BusinessRating expectedBusinessRating) {
        assertBusinessRatingAllUpdatablePropertiesEquals(expectedBusinessRating, getPersistedBusinessRating(expectedBusinessRating));
    }
}
