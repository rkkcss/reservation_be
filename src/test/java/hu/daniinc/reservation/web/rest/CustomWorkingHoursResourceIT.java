package hu.daniinc.reservation.web.rest;

import static hu.daniinc.reservation.domain.CustomWorkingHoursAsserts.*;
import static hu.daniinc.reservation.web.rest.TestUtil.createUpdateProxyForBean;
import static hu.daniinc.reservation.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.IntegrationTest;
import hu.daniinc.reservation.domain.CustomWorkingHours;
import hu.daniinc.reservation.repository.CustomWorkingHoursRepository;
import hu.daniinc.reservation.service.dto.CustomWorkingHoursDTO;
import hu.daniinc.reservation.service.mapper.CustomWorkingHoursMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
 * Integration tests for the {@link CustomWorkingHoursResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CustomWorkingHoursResourceIT {

    private static final LocalDate DEFAULT_WORK_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_WORK_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final ZonedDateTime DEFAULT_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/custom-working-hours";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CustomWorkingHoursRepository customWorkingHoursRepository;

    @Autowired
    private CustomWorkingHoursMapper customWorkingHoursMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCustomWorkingHoursMockMvc;

    private CustomWorkingHours customWorkingHours;

    private CustomWorkingHours insertedCustomWorkingHours;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CustomWorkingHours createEntity() {
        return new CustomWorkingHours()
            .workDate(DEFAULT_WORK_DATE)
            .startTime(DEFAULT_START_TIME.toInstant())
            .endTime(DEFAULT_END_TIME.toInstant());
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CustomWorkingHours createUpdatedEntity() {
        return new CustomWorkingHours()
            .workDate(UPDATED_WORK_DATE)
            .startTime(UPDATED_START_TIME.toInstant())
            .endTime(UPDATED_END_TIME.toInstant());
    }

    @BeforeEach
    public void initTest() {
        customWorkingHours = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedCustomWorkingHours != null) {
            customWorkingHoursRepository.delete(insertedCustomWorkingHours);
            insertedCustomWorkingHours = null;
        }
    }

    @Test
    @Transactional
    void createCustomWorkingHours() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CustomWorkingHours
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);
        var returnedCustomWorkingHoursDTO = om.readValue(
            restCustomWorkingHoursMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(customWorkingHoursDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CustomWorkingHoursDTO.class
        );

        // Validate the CustomWorkingHours in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCustomWorkingHours = customWorkingHoursMapper.toEntity(returnedCustomWorkingHoursDTO);
        assertCustomWorkingHoursUpdatableFieldsEquals(
            returnedCustomWorkingHours,
            getPersistedCustomWorkingHours(returnedCustomWorkingHours)
        );

        insertedCustomWorkingHours = returnedCustomWorkingHours;
    }

    @Test
    @Transactional
    void createCustomWorkingHoursWithExistingId() throws Exception {
        // Create the CustomWorkingHours with an existing ID
        customWorkingHours.setId(1L);
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCustomWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkWorkDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customWorkingHours.setWorkDate(null);

        // Create the CustomWorkingHours, which fails.
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        restCustomWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customWorkingHours.setStartTime(null);

        // Create the CustomWorkingHours, which fails.
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        restCustomWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customWorkingHours.setEndTime(null);

        // Create the CustomWorkingHours, which fails.
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        restCustomWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCustomWorkingHours() throws Exception {
        // Initialize the database
        insertedCustomWorkingHours = customWorkingHoursRepository.saveAndFlush(customWorkingHours);

        // Get all the customWorkingHoursList
        restCustomWorkingHoursMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customWorkingHours.getId().intValue())))
            .andExpect(jsonPath("$.[*].workDate").value(hasItem(DEFAULT_WORK_DATE.toString())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(sameInstant(DEFAULT_START_TIME))))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(sameInstant(DEFAULT_END_TIME))));
    }

    @Test
    @Transactional
    void getCustomWorkingHours() throws Exception {
        // Initialize the database
        insertedCustomWorkingHours = customWorkingHoursRepository.saveAndFlush(customWorkingHours);

        // Get the customWorkingHours
        restCustomWorkingHoursMockMvc
            .perform(get(ENTITY_API_URL_ID, customWorkingHours.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(customWorkingHours.getId().intValue()))
            .andExpect(jsonPath("$.workDate").value(DEFAULT_WORK_DATE.toString()))
            .andExpect(jsonPath("$.startTime").value(sameInstant(DEFAULT_START_TIME)))
            .andExpect(jsonPath("$.endTime").value(sameInstant(DEFAULT_END_TIME)));
    }

    @Test
    @Transactional
    void getNonExistingCustomWorkingHours() throws Exception {
        // Get the customWorkingHours
        restCustomWorkingHoursMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCustomWorkingHours() throws Exception {
        // Initialize the database
        insertedCustomWorkingHours = customWorkingHoursRepository.saveAndFlush(customWorkingHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the customWorkingHours
        CustomWorkingHours updatedCustomWorkingHours = customWorkingHoursRepository.findById(customWorkingHours.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCustomWorkingHours are not directly saved in db
        em.detach(updatedCustomWorkingHours);
        updatedCustomWorkingHours
            .workDate(UPDATED_WORK_DATE)
            .startTime(UPDATED_START_TIME.toInstant())
            .endTime(UPDATED_END_TIME.toInstant());
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(updatedCustomWorkingHours);

        restCustomWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, customWorkingHoursDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isOk());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCustomWorkingHoursToMatchAllProperties(updatedCustomWorkingHours);
    }

    @Test
    @Transactional
    void putNonExistingCustomWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customWorkingHours.setId(longCount.incrementAndGet());

        // Create the CustomWorkingHours
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, customWorkingHoursDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCustomWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customWorkingHours.setId(longCount.incrementAndGet());

        // Create the CustomWorkingHours
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCustomWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customWorkingHours.setId(longCount.incrementAndGet());

        // Create the CustomWorkingHours
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCustomWorkingHoursWithPatch() throws Exception {
        // Initialize the database
        insertedCustomWorkingHours = customWorkingHoursRepository.saveAndFlush(customWorkingHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the customWorkingHours using partial update
        CustomWorkingHours partialUpdatedCustomWorkingHours = new CustomWorkingHours();
        partialUpdatedCustomWorkingHours.setId(customWorkingHours.getId());

        partialUpdatedCustomWorkingHours.endTime(UPDATED_END_TIME.toInstant());

        restCustomWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCustomWorkingHours.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCustomWorkingHours))
            )
            .andExpect(status().isOk());

        // Validate the CustomWorkingHours in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCustomWorkingHoursUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCustomWorkingHours, customWorkingHours),
            getPersistedCustomWorkingHours(customWorkingHours)
        );
    }

    @Test
    @Transactional
    void fullUpdateCustomWorkingHoursWithPatch() throws Exception {
        // Initialize the database
        insertedCustomWorkingHours = customWorkingHoursRepository.saveAndFlush(customWorkingHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the customWorkingHours using partial update
        CustomWorkingHours partialUpdatedCustomWorkingHours = new CustomWorkingHours();
        partialUpdatedCustomWorkingHours.setId(customWorkingHours.getId());

        partialUpdatedCustomWorkingHours
            .workDate(UPDATED_WORK_DATE)
            .startTime(UPDATED_START_TIME.toInstant())
            .endTime(UPDATED_END_TIME.toInstant());

        restCustomWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCustomWorkingHours.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCustomWorkingHours))
            )
            .andExpect(status().isOk());

        // Validate the CustomWorkingHours in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCustomWorkingHoursUpdatableFieldsEquals(
            partialUpdatedCustomWorkingHours,
            getPersistedCustomWorkingHours(partialUpdatedCustomWorkingHours)
        );
    }

    @Test
    @Transactional
    void patchNonExistingCustomWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customWorkingHours.setId(longCount.incrementAndGet());

        // Create the CustomWorkingHours
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, customWorkingHoursDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCustomWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customWorkingHours.setId(longCount.incrementAndGet());

        // Create the CustomWorkingHours
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCustomWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customWorkingHours.setId(longCount.incrementAndGet());

        // Create the CustomWorkingHours
        CustomWorkingHoursDTO customWorkingHoursDTO = customWorkingHoursMapper.toDto(customWorkingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(customWorkingHoursDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CustomWorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCustomWorkingHours() throws Exception {
        // Initialize the database
        insertedCustomWorkingHours = customWorkingHoursRepository.saveAndFlush(customWorkingHours);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the customWorkingHours
        restCustomWorkingHoursMockMvc
            .perform(delete(ENTITY_API_URL_ID, customWorkingHours.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return customWorkingHoursRepository.count();
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

    protected CustomWorkingHours getPersistedCustomWorkingHours(CustomWorkingHours customWorkingHours) {
        return customWorkingHoursRepository.findById(customWorkingHours.getId()).orElseThrow();
    }

    protected void assertPersistedCustomWorkingHoursToMatchAllProperties(CustomWorkingHours expectedCustomWorkingHours) {
        assertCustomWorkingHoursAllPropertiesEquals(expectedCustomWorkingHours, getPersistedCustomWorkingHours(expectedCustomWorkingHours));
    }

    protected void assertPersistedCustomWorkingHoursToMatchUpdatableProperties(CustomWorkingHours expectedCustomWorkingHours) {
        assertCustomWorkingHoursAllUpdatablePropertiesEquals(
            expectedCustomWorkingHours,
            getPersistedCustomWorkingHours(expectedCustomWorkingHours)
        );
    }
}
