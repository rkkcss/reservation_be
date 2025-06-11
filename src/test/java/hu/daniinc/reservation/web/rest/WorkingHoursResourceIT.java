package hu.daniinc.reservation.web.rest;

import static hu.daniinc.reservation.domain.WorkingHoursAsserts.*;
import static hu.daniinc.reservation.web.rest.TestUtil.createUpdateProxyForBean;
import static hu.daniinc.reservation.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.IntegrationTest;
import hu.daniinc.reservation.domain.WorkingHours;
import hu.daniinc.reservation.repository.WorkingHoursRepository;
import hu.daniinc.reservation.service.dto.WorkingHoursDTO;
import hu.daniinc.reservation.service.mapper.WorkingHoursMapper;
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
 * Integration tests for the {@link WorkingHoursResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WorkingHoursResourceIT {

    private static final Integer DEFAULT_DAY_OF_WEEK = 1;
    private static final Integer UPDATED_DAY_OF_WEEK = 2;

    private static final ZonedDateTime DEFAULT_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/working-hours";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @Autowired
    private WorkingHoursMapper workingHoursMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWorkingHoursMockMvc;

    private WorkingHours workingHours;

    private WorkingHours insertedWorkingHours;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WorkingHours createEntity() {
        return new WorkingHours()
            .dayOfWeek(DEFAULT_DAY_OF_WEEK)
            .startTime(LocalTime.from(DEFAULT_START_TIME))
            .endTime(LocalTime.from(DEFAULT_END_TIME));
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WorkingHours createUpdatedEntity() {
        return new WorkingHours()
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(LocalTime.from(UPDATED_START_TIME))
            .endTime(LocalTime.from(UPDATED_END_TIME));
    }

    @BeforeEach
    public void initTest() {
        workingHours = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedWorkingHours != null) {
            workingHoursRepository.delete(insertedWorkingHours);
            insertedWorkingHours = null;
        }
    }

    @Test
    @Transactional
    void createWorkingHours() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the WorkingHours
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);
        var returnedWorkingHoursDTO = om.readValue(
            restWorkingHoursMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workingHoursDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            WorkingHoursDTO.class
        );

        // Validate the WorkingHours in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedWorkingHours = workingHoursMapper.toEntity(returnedWorkingHoursDTO);
        assertWorkingHoursUpdatableFieldsEquals(returnedWorkingHours, getPersistedWorkingHours(returnedWorkingHours));

        insertedWorkingHours = returnedWorkingHours;
    }

    @Test
    @Transactional
    void createWorkingHoursWithExistingId() throws Exception {
        // Create the WorkingHours with an existing ID
        workingHours.setId(1L);
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDayOfWeekIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        workingHours.setDayOfWeek(null);

        // Create the WorkingHours, which fails.
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        restWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        workingHours.setStartTime(null);

        // Create the WorkingHours, which fails.
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        restWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        workingHours.setEndTime(null);

        // Create the WorkingHours, which fails.
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        restWorkingHoursMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllWorkingHours() throws Exception {
        // Initialize the database
        insertedWorkingHours = workingHoursRepository.saveAndFlush(workingHours);

        // Get all the workingHoursList
        restWorkingHoursMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(workingHours.getId().intValue())))
            .andExpect(jsonPath("$.[*].dayOfWeek").value(hasItem(DEFAULT_DAY_OF_WEEK)))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(sameInstant(DEFAULT_START_TIME))))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(sameInstant(DEFAULT_END_TIME))));
    }

    @Test
    @Transactional
    void getWorkingHours() throws Exception {
        // Initialize the database
        insertedWorkingHours = workingHoursRepository.saveAndFlush(workingHours);

        // Get the workingHours
        restWorkingHoursMockMvc
            .perform(get(ENTITY_API_URL_ID, workingHours.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(workingHours.getId().intValue()))
            .andExpect(jsonPath("$.dayOfWeek").value(DEFAULT_DAY_OF_WEEK))
            .andExpect(jsonPath("$.startTime").value(sameInstant(DEFAULT_START_TIME)))
            .andExpect(jsonPath("$.endTime").value(sameInstant(DEFAULT_END_TIME)));
    }

    @Test
    @Transactional
    void getNonExistingWorkingHours() throws Exception {
        // Get the workingHours
        restWorkingHoursMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWorkingHours() throws Exception {
        // Initialize the database
        insertedWorkingHours = workingHoursRepository.saveAndFlush(workingHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the workingHours
        WorkingHours updatedWorkingHours = workingHoursRepository.findById(workingHours.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedWorkingHours are not directly saved in db
        em.detach(updatedWorkingHours);
        updatedWorkingHours
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(LocalTime.from(UPDATED_START_TIME))
            .endTime(LocalTime.from(UPDATED_END_TIME));
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(updatedWorkingHours);

        restWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, workingHoursDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isOk());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedWorkingHoursToMatchAllProperties(updatedWorkingHours);
    }

    @Test
    @Transactional
    void putNonExistingWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workingHours.setId(longCount.incrementAndGet());

        // Create the WorkingHours
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, workingHoursDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workingHours.setId(longCount.incrementAndGet());

        // Create the WorkingHours
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workingHours.setId(longCount.incrementAndGet());

        // Create the WorkingHours
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkingHoursMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWorkingHoursWithPatch() throws Exception {
        // Initialize the database
        insertedWorkingHours = workingHoursRepository.saveAndFlush(workingHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the workingHours using partial update
        WorkingHours partialUpdatedWorkingHours = new WorkingHours();
        partialUpdatedWorkingHours.setId(workingHours.getId());

        partialUpdatedWorkingHours
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(LocalTime.from(UPDATED_START_TIME))
            .endTime(LocalTime.from(UPDATED_END_TIME));

        restWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorkingHours.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWorkingHours))
            )
            .andExpect(status().isOk());

        // Validate the WorkingHours in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWorkingHoursUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedWorkingHours, workingHours),
            getPersistedWorkingHours(workingHours)
        );
    }

    @Test
    @Transactional
    void fullUpdateWorkingHoursWithPatch() throws Exception {
        // Initialize the database
        insertedWorkingHours = workingHoursRepository.saveAndFlush(workingHours);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the workingHours using partial update
        WorkingHours partialUpdatedWorkingHours = new WorkingHours();
        partialUpdatedWorkingHours.setId(workingHours.getId());

        partialUpdatedWorkingHours
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(LocalTime.from(UPDATED_START_TIME))
            .endTime(LocalTime.from(UPDATED_END_TIME));

        restWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorkingHours.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWorkingHours))
            )
            .andExpect(status().isOk());

        // Validate the WorkingHours in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWorkingHoursUpdatableFieldsEquals(partialUpdatedWorkingHours, getPersistedWorkingHours(partialUpdatedWorkingHours));
    }

    @Test
    @Transactional
    void patchNonExistingWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workingHours.setId(longCount.incrementAndGet());

        // Create the WorkingHours
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, workingHoursDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workingHours.setId(longCount.incrementAndGet());

        // Create the WorkingHours
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWorkingHours() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workingHours.setId(longCount.incrementAndGet());

        // Create the WorkingHours
        WorkingHoursDTO workingHoursDTO = workingHoursMapper.toDto(workingHours);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkingHoursMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(workingHoursDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the WorkingHours in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWorkingHours() throws Exception {
        // Initialize the database
        insertedWorkingHours = workingHoursRepository.saveAndFlush(workingHours);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the workingHours
        restWorkingHoursMockMvc
            .perform(delete(ENTITY_API_URL_ID, workingHours.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return workingHoursRepository.count();
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

    protected WorkingHours getPersistedWorkingHours(WorkingHours workingHours) {
        return workingHoursRepository.findById(workingHours.getId()).orElseThrow();
    }

    protected void assertPersistedWorkingHoursToMatchAllProperties(WorkingHours expectedWorkingHours) {
        assertWorkingHoursAllPropertiesEquals(expectedWorkingHours, getPersistedWorkingHours(expectedWorkingHours));
    }

    protected void assertPersistedWorkingHoursToMatchUpdatableProperties(WorkingHours expectedWorkingHours) {
        assertWorkingHoursAllUpdatablePropertiesEquals(expectedWorkingHours, getPersistedWorkingHours(expectedWorkingHours));
    }
}
