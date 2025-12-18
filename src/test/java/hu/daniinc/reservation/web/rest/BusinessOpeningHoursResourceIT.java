package hu.daniinc.reservation.web.rest;

import static hu.daniinc.reservation.domain.BusinessOpeningHoursAsserts.*;
import static hu.daniinc.reservation.web.rest.TestUtil.createUpdateProxyForBean;
import static hu.daniinc.reservation.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.IntegrationTest;
import hu.daniinc.reservation.domain.BusinessOpeningHours;
import hu.daniinc.reservation.repository.BusinessOpeningHoursRepository;
import hu.daniinc.reservation.service.dto.BusinessOpeningHoursDTO;
import hu.daniinc.reservation.service.mapper.BusinessOpeningHoursMapper;
import jakarta.persistence.EntityManager;
import java.time.*;
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
 * Integration tests for the {@link BusinessOpeningHoursResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BusinessOpeningHoursResourceIT {

    private static final Integer DEFAULT_DAY_OF_WEEK = 1;
    private static final Integer UPDATED_DAY_OF_WEEK = 2;

    private static final LocalTime DEFAULT_START_TIME = LocalTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final LocalTime UPDATED_START_TIME = LocalTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/business-opening-hours";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BusinessOpeningHoursRepository businessOpeningHoursRepository;

    @Autowired
    private BusinessOpeningHoursMapper businessOpeningHoursMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBusinessOpeningHoursMockMvc;

    private BusinessOpeningHours businessOpeningHours;

    private BusinessOpeningHours insertedBusinessOpeningHours;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BusinessOpeningHours createEntity() {
        return new BusinessOpeningHours()
            .dayOfWeek(DEFAULT_DAY_OF_WEEK)
            .startTime(DEFAULT_START_TIME)
            .endTime(LocalTime.from(DEFAULT_END_TIME));
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BusinessOpeningHours createUpdatedEntity() {
        return new BusinessOpeningHours()
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(UPDATED_START_TIME)
            .endTime(LocalTime.from(UPDATED_END_TIME));
    }

    @BeforeEach
    public void initTest() {
        businessOpeningHours = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedBusinessOpeningHours != null) {
            businessOpeningHoursRepository.delete(insertedBusinessOpeningHours);
            insertedBusinessOpeningHours = null;
        }
    }

    @Test
    @Transactional
    void createBusinessOpeningHours() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BusinessOpeningHours
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);
        var returnedBusinessOpeningHoursDTO = om.readValue(
            restBusinessOpeningHoursMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(businessOpeningHoursDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BusinessOpeningHoursDTO.class
        );

        // Validate the BusinessOpeningHours in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBusinessOpeningHours = businessOpeningHoursMapper.toEntity(returnedBusinessOpeningHoursDTO);
        assertBusinessOpeningHoursUpdatableFieldsEquals(
            returnedBusinessOpeningHours,
            getPersistedBusinessOpeningHours(returnedBusinessOpeningHours)
        );

        insertedBusinessOpeningHours = returnedBusinessOpeningHours;
    }

    @Test
    @Transactional
    void createBusinessOpeningHoursWithExistingId() throws Exception {
        // Create the BusinessOpeningHours with an existing ID
        businessOpeningHours.setId(1L);
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBusinessOpeningHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDayOfWeekIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        businessOpeningHours.setDayOfWeek(null);

        // Create the BusinessOpeningHours, which fails.
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        restBusinessOpeningHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        businessOpeningHours.setStartTime(null);

        // Create the BusinessOpeningHours, which fails.
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        restBusinessOpeningHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        businessOpeningHours.setEndTime(null);

        // Create the BusinessOpeningHours, which fails.
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        restBusinessOpeningHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBusinessOpeningHours() throws Exception {
        // Initialize the database
        insertedBusinessOpeningHours = businessOpeningHoursRepository.saveAndFlush(businessOpeningHours);

        // Get all the businessOpeningHoursList
        restBusinessOpeningHoursMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(businessOpeningHours.getId().intValue())))
            .andExpect(jsonPath("$.[*].dayOfWeek").value(hasItem(DEFAULT_DAY_OF_WEEK)))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(sameInstant(ZonedDateTime.from(DEFAULT_START_TIME)))))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(sameInstant(DEFAULT_END_TIME))));
    }

    @Test
    @Transactional
    void getBusinessOpeningHours() throws Exception {
        // Initialize the database
        insertedBusinessOpeningHours = businessOpeningHoursRepository.saveAndFlush(businessOpeningHours);

        // Get the businessOpeningHours
        restBusinessOpeningHoursMockMvc
            .perform(get(ENTITY_API_URL_ID, businessOpeningHours.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(businessOpeningHours.getId().intValue()))
            .andExpect(jsonPath("$.dayOfWeek").value(DEFAULT_DAY_OF_WEEK))
            .andExpect(jsonPath("$.startTime").value(sameInstant(ZonedDateTime.from(DEFAULT_START_TIME))))
            .andExpect(jsonPath("$.endTime").value(sameInstant(DEFAULT_END_TIME)));
    }

    @Test
    @Transactional
    void getNonExistingBusinessOpeningHours() throws Exception {
        // Get the businessOpeningHours
        restBusinessOpeningHoursMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBusinessOpeningHours() throws Exception {
        // Initialize the database
        insertedBusinessOpeningHours = businessOpeningHoursRepository.saveAndFlush(businessOpeningHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the businessOpeningHours
        BusinessOpeningHours updatedBusinessOpeningHours = businessOpeningHoursRepository
            .findById(businessOpeningHours.getId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedBusinessOpeningHours are not directly saved in db
        em.detach(updatedBusinessOpeningHours);
        updatedBusinessOpeningHours.dayOfWeek(UPDATED_DAY_OF_WEEK).startTime(UPDATED_START_TIME).endTime(LocalTime.from(UPDATED_END_TIME));
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(updatedBusinessOpeningHours);

        restBusinessOpeningHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, businessOpeningHoursDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isOk());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBusinessOpeningHoursToMatchAllProperties(updatedBusinessOpeningHours);
    }

    @Test
    @Transactional
    void putNonExistingBusinessOpeningHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessOpeningHours.setId(longCount.incrementAndGet());

        // Create the BusinessOpeningHours
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBusinessOpeningHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, businessOpeningHoursDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBusinessOpeningHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessOpeningHours.setId(longCount.incrementAndGet());

        // Create the BusinessOpeningHours
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessOpeningHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBusinessOpeningHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessOpeningHours.setId(longCount.incrementAndGet());

        // Create the BusinessOpeningHours
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessOpeningHoursMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBusinessOpeningHoursWithPatch() throws Exception {
        // Initialize the database
        insertedBusinessOpeningHours = businessOpeningHoursRepository.saveAndFlush(businessOpeningHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the businessOpeningHours using partial update
        BusinessOpeningHours partialUpdatedBusinessOpeningHours = new BusinessOpeningHours();
        partialUpdatedBusinessOpeningHours.setId(businessOpeningHours.getId());

        partialUpdatedBusinessOpeningHours.dayOfWeek(UPDATED_DAY_OF_WEEK);

        restBusinessOpeningHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBusinessOpeningHours.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBusinessOpeningHours))
            )
            .andExpect(status().isOk());

        // Validate the BusinessOpeningHours in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBusinessOpeningHoursUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBusinessOpeningHours, businessOpeningHours),
            getPersistedBusinessOpeningHours(businessOpeningHours)
        );
    }

    @Test
    @Transactional
    void fullUpdateBusinessOpeningHoursWithPatch() throws Exception {
        // Initialize the database
        insertedBusinessOpeningHours = businessOpeningHoursRepository.saveAndFlush(businessOpeningHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the businessOpeningHours using partial update
        BusinessOpeningHours partialUpdatedBusinessOpeningHours = new BusinessOpeningHours();
        partialUpdatedBusinessOpeningHours.setId(businessOpeningHours.getId());

        partialUpdatedBusinessOpeningHours
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(UPDATED_START_TIME)
            .endTime(LocalTime.from(UPDATED_END_TIME));

        restBusinessOpeningHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBusinessOpeningHours.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBusinessOpeningHours))
            )
            .andExpect(status().isOk());

        // Validate the BusinessOpeningHours in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBusinessOpeningHoursUpdatableFieldsEquals(
            partialUpdatedBusinessOpeningHours,
            getPersistedBusinessOpeningHours(partialUpdatedBusinessOpeningHours)
        );
    }

    @Test
    @Transactional
    void patchNonExistingBusinessOpeningHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessOpeningHours.setId(longCount.incrementAndGet());

        // Create the BusinessOpeningHours
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBusinessOpeningHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, businessOpeningHoursDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBusinessOpeningHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessOpeningHours.setId(longCount.incrementAndGet());

        // Create the BusinessOpeningHours
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessOpeningHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBusinessOpeningHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        businessOpeningHours.setId(longCount.incrementAndGet());

        // Create the BusinessOpeningHours
        BusinessOpeningHoursDTO businessOpeningHoursDTO = businessOpeningHoursMapper.toDto(businessOpeningHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessOpeningHoursMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessOpeningHoursDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the BusinessOpeningHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBusinessOpeningHours() throws Exception {
        // Initialize the database
        insertedBusinessOpeningHours = businessOpeningHoursRepository.saveAndFlush(businessOpeningHours);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the businessOpeningHours
        restBusinessOpeningHoursMockMvc
            .perform(delete(ENTITY_API_URL_ID, businessOpeningHours.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return businessOpeningHoursRepository.count();
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

    protected BusinessOpeningHours getPersistedBusinessOpeningHours(BusinessOpeningHours businessOpeningHours) {
        return businessOpeningHoursRepository.findById(businessOpeningHours.getId()).orElseThrow();
    }

    protected void assertPersistedBusinessOpeningHoursToMatchAllProperties(BusinessOpeningHours expectedBusinessOpeningHours) {
        assertBusinessOpeningHoursAllPropertiesEquals(
            expectedBusinessOpeningHours,
            getPersistedBusinessOpeningHours(expectedBusinessOpeningHours)
        );
    }

    protected void assertPersistedBusinessOpeningHoursToMatchUpdatableProperties(BusinessOpeningHours expectedBusinessOpeningHours) {
        assertBusinessOpeningHoursAllUpdatablePropertiesEquals(
            expectedBusinessOpeningHours,
            getPersistedBusinessOpeningHours(expectedBusinessOpeningHours)
        );
    }
}
