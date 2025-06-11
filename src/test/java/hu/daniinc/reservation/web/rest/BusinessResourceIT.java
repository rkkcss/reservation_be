package hu.daniinc.reservation.web.rest;

import static hu.daniinc.reservation.domain.BusinessAsserts.*;
import static hu.daniinc.reservation.web.rest.TestUtil.createUpdateProxyForBean;
import static hu.daniinc.reservation.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.IntegrationTest;
import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.repository.UserRepository;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.mapper.BusinessMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
 * Integration tests for the {@link BusinessResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BusinessResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final Integer DEFAULT_BREAK_BETWEEN_APPOINTMENTS_MIN = 0;
    private static final Integer UPDATED_BREAK_BETWEEN_APPOINTMENTS_MIN = 1;

    private static final String DEFAULT_LOGO = "AAAAAAAAAA";
    private static final String UPDATED_LOGO = "BBBBBBBBBB";

    private static final String DEFAULT_BANNER_URL = "AAAAAAAAAA";
    private static final String UPDATED_BANNER_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/businesses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBusinessMockMvc;

    private Business business;

    private Business insertedBusiness;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Business createEntity() {
        return new Business()
            .name(DEFAULT_NAME)
            .createdDate(DEFAULT_CREATED_DATE)
            .description(DEFAULT_DESCRIPTION)
            .address(DEFAULT_ADDRESS)
            .phoneNumber(DEFAULT_PHONE_NUMBER)
            .breakBetweenAppointmentsMin(DEFAULT_BREAK_BETWEEN_APPOINTMENTS_MIN)
            .logo(DEFAULT_LOGO)
            .bannerUrl(DEFAULT_BANNER_URL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Business createUpdatedEntity() {
        return new Business()
            .name(UPDATED_NAME)
            .createdDate(UPDATED_CREATED_DATE)
            .description(UPDATED_DESCRIPTION)
            .address(UPDATED_ADDRESS)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .breakBetweenAppointmentsMin(UPDATED_BREAK_BETWEEN_APPOINTMENTS_MIN)
            .logo(UPDATED_LOGO)
            .bannerUrl(UPDATED_BANNER_URL);
    }

    @BeforeEach
    public void initTest() {
        business = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedBusiness != null) {
            businessRepository.delete(insertedBusiness);
            insertedBusiness = null;
        }
    }

    @Test
    @Transactional
    void createBusiness() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Business
        BusinessDTO businessDTO = businessMapper.toDto(business);
        var returnedBusinessDTO = om.readValue(
            restBusinessMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(businessDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BusinessDTO.class
        );

        // Validate the Business in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBusiness = businessMapper.toEntity(returnedBusinessDTO);
        assertBusinessUpdatableFieldsEquals(returnedBusiness, getPersistedBusiness(returnedBusiness));

        insertedBusiness = returnedBusiness;
    }

    @Test
    @Transactional
    void createBusinessWithExistingId() throws Exception {
        // Create the Business with an existing ID
        business.setId(1L);
        BusinessDTO businessDTO = businessMapper.toDto(business);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBusinessMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(businessDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        business.setName(null);

        // Create the Business, which fails.
        BusinessDTO businessDTO = businessMapper.toDto(business);

        restBusinessMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(businessDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBusinesses() throws Exception {
        // Initialize the database
        insertedBusiness = businessRepository.saveAndFlush(business);

        // Get all the businessList
        restBusinessMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(business.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(sameInstant(DEFAULT_CREATED_DATE))))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].breakBetweenAppointmentsMin").value(hasItem(DEFAULT_BREAK_BETWEEN_APPOINTMENTS_MIN)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(DEFAULT_LOGO)))
            .andExpect(jsonPath("$.[*].bannerUrl").value(hasItem(DEFAULT_BANNER_URL)));
    }

    @Test
    @Transactional
    void getBusiness() throws Exception {
        // Initialize the database
        insertedBusiness = businessRepository.saveAndFlush(business);

        // Get the business
        restBusinessMockMvc
            .perform(get(ENTITY_API_URL_ID, business.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(business.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.createdDate").value(sameInstant(DEFAULT_CREATED_DATE)))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER))
            .andExpect(jsonPath("$.breakBetweenAppointmentsMin").value(DEFAULT_BREAK_BETWEEN_APPOINTMENTS_MIN))
            .andExpect(jsonPath("$.logo").value(DEFAULT_LOGO))
            .andExpect(jsonPath("$.bannerUrl").value(DEFAULT_BANNER_URL));
    }

    @Test
    @Transactional
    void getNonExistingBusiness() throws Exception {
        // Get the business
        restBusinessMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBusiness() throws Exception {
        // Initialize the database
        insertedBusiness = businessRepository.saveAndFlush(business);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the business
        Business updatedBusiness = businessRepository.findById(business.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBusiness are not directly saved in db
        em.detach(updatedBusiness);
        updatedBusiness
            .name(UPDATED_NAME)
            .createdDate(UPDATED_CREATED_DATE)
            .description(UPDATED_DESCRIPTION)
            .address(UPDATED_ADDRESS)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .breakBetweenAppointmentsMin(UPDATED_BREAK_BETWEEN_APPOINTMENTS_MIN)
            .logo(UPDATED_LOGO)
            .bannerUrl(UPDATED_BANNER_URL);
        BusinessDTO businessDTO = businessMapper.toDto(updatedBusiness);

        restBusinessMockMvc
            .perform(
                put(ENTITY_API_URL_ID, businessDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessDTO))
            )
            .andExpect(status().isOk());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBusinessToMatchAllProperties(updatedBusiness);
    }

    @Test
    @Transactional
    void putNonExistingBusiness() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        business.setId(longCount.incrementAndGet());

        // Create the Business
        BusinessDTO businessDTO = businessMapper.toDto(business);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBusinessMockMvc
            .perform(
                put(ENTITY_API_URL_ID, businessDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBusiness() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        business.setId(longCount.incrementAndGet());

        // Create the Business
        BusinessDTO businessDTO = businessMapper.toDto(business);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(businessDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBusiness() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        business.setId(longCount.incrementAndGet());

        // Create the Business
        BusinessDTO businessDTO = businessMapper.toDto(business);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(businessDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBusinessWithPatch() throws Exception {
        // Initialize the database
        insertedBusiness = businessRepository.saveAndFlush(business);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the business using partial update
        Business partialUpdatedBusiness = new Business();
        partialUpdatedBusiness.setId(business.getId());

        partialUpdatedBusiness
            .createdDate(UPDATED_CREATED_DATE)
            .description(UPDATED_DESCRIPTION)
            .address(UPDATED_ADDRESS)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .breakBetweenAppointmentsMin(UPDATED_BREAK_BETWEEN_APPOINTMENTS_MIN)
            .logo(UPDATED_LOGO)
            .bannerUrl(UPDATED_BANNER_URL);

        restBusinessMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBusiness.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBusiness))
            )
            .andExpect(status().isOk());

        // Validate the Business in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBusinessUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBusiness, business), getPersistedBusiness(business));
    }

    @Test
    @Transactional
    void fullUpdateBusinessWithPatch() throws Exception {
        // Initialize the database
        insertedBusiness = businessRepository.saveAndFlush(business);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the business using partial update
        Business partialUpdatedBusiness = new Business();
        partialUpdatedBusiness.setId(business.getId());

        partialUpdatedBusiness
            .name(UPDATED_NAME)
            .createdDate(UPDATED_CREATED_DATE)
            .description(UPDATED_DESCRIPTION)
            .address(UPDATED_ADDRESS)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .breakBetweenAppointmentsMin(UPDATED_BREAK_BETWEEN_APPOINTMENTS_MIN)
            .logo(UPDATED_LOGO)
            .bannerUrl(UPDATED_BANNER_URL);

        restBusinessMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBusiness.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBusiness))
            )
            .andExpect(status().isOk());

        // Validate the Business in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBusinessUpdatableFieldsEquals(partialUpdatedBusiness, getPersistedBusiness(partialUpdatedBusiness));
    }

    @Test
    @Transactional
    void patchNonExistingBusiness() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        business.setId(longCount.incrementAndGet());

        // Create the Business
        BusinessDTO businessDTO = businessMapper.toDto(business);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBusinessMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, businessDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBusiness() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        business.setId(longCount.incrementAndGet());

        // Create the Business
        BusinessDTO businessDTO = businessMapper.toDto(business);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(businessDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBusiness() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        business.setId(longCount.incrementAndGet());

        // Create the Business
        BusinessDTO businessDTO = businessMapper.toDto(business);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBusinessMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(businessDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Business in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBusiness() throws Exception {
        // Initialize the database
        insertedBusiness = businessRepository.saveAndFlush(business);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the business
        restBusinessMockMvc
            .perform(delete(ENTITY_API_URL_ID, business.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return businessRepository.count();
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

    protected Business getPersistedBusiness(Business business) {
        return businessRepository.findById(business.getId()).orElseThrow();
    }

    protected void assertPersistedBusinessToMatchAllProperties(Business expectedBusiness) {
        assertBusinessAllPropertiesEquals(expectedBusiness, getPersistedBusiness(expectedBusiness));
    }

    protected void assertPersistedBusinessToMatchUpdatableProperties(Business expectedBusiness) {
        assertBusinessAllUpdatablePropertiesEquals(expectedBusiness, getPersistedBusiness(expectedBusiness));
    }
}
