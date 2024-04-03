package com.groupeisi.web.rest;

import static com.groupeisi.domain.SearchHistoryAsserts.*;
//import static com.groupeisi.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupeisi.IntegrationTest;
import com.groupeisi.domain.SearchHistory;
import com.groupeisi.repository.SearchHistoryRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SearchHistoryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SearchHistoryResourceIT {

    private static final LocalDate DEFAULT_SEARCHED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_SEARCHED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_DAY_OF_WEEK = "AAAAAAAAAA";
    private static final String UPDATED_DAY_OF_WEEK = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/search-histories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSearchHistoryMockMvc;

    private SearchHistory searchHistory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SearchHistory createEntity(EntityManager em) {
        SearchHistory searchHistory = new SearchHistory().searchedDate(DEFAULT_SEARCHED_DATE).dayOfWeek(DEFAULT_DAY_OF_WEEK);
        return searchHistory;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SearchHistory createUpdatedEntity(EntityManager em) {
        SearchHistory searchHistory = new SearchHistory().searchedDate(UPDATED_SEARCHED_DATE).dayOfWeek(UPDATED_DAY_OF_WEEK);
        return searchHistory;
    }

    @BeforeEach
    public void initTest() {
        searchHistory = createEntity(em);
    }

    @Test
    @Transactional
    void createSearchHistory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SearchHistory
        var returnedSearchHistory = om.readValue(
            restSearchHistoryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(searchHistory)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SearchHistory.class
        );

        // Validate the SearchHistory in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSearchHistoryUpdatableFieldsEquals(returnedSearchHistory, getPersistedSearchHistory(returnedSearchHistory));
    }

    @Test
    @Transactional
    void createSearchHistoryWithExistingId() throws Exception {
        // Create the SearchHistory with an existing ID
        searchHistory.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSearchHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(searchHistory)))
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllSearchHistories() throws Exception {
        // Initialize the database
        searchHistoryRepository.saveAndFlush(searchHistory);

        // Get all the searchHistoryList
        restSearchHistoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(searchHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].searchedDate").value(hasItem(DEFAULT_SEARCHED_DATE.toString())))
            .andExpect(jsonPath("$.[*].dayOfWeek").value(hasItem(DEFAULT_DAY_OF_WEEK)));
    }

    @Test
    @Transactional
    void getSearchHistory() throws Exception {
        // Initialize the database
        searchHistoryRepository.saveAndFlush(searchHistory);

        // Get the searchHistory
        restSearchHistoryMockMvc
            .perform(get(ENTITY_API_URL_ID, searchHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(searchHistory.getId().intValue()))
            .andExpect(jsonPath("$.searchedDate").value(DEFAULT_SEARCHED_DATE.toString()))
            .andExpect(jsonPath("$.dayOfWeek").value(DEFAULT_DAY_OF_WEEK));
    }

    @Test
    @Transactional
    void getNonExistingSearchHistory() throws Exception {
        // Get the searchHistory
        restSearchHistoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSearchHistory() throws Exception {
        // Initialize the database
        searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the searchHistory
        SearchHistory updatedSearchHistory = searchHistoryRepository.findById(searchHistory.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSearchHistory are not directly saved in db
        em.detach(updatedSearchHistory);
        updatedSearchHistory.searchedDate(UPDATED_SEARCHED_DATE).dayOfWeek(UPDATED_DAY_OF_WEEK);

        restSearchHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSearchHistory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSearchHistory))
            )
            .andExpect(status().isOk());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSearchHistoryToMatchAllProperties(updatedSearchHistory);
    }

    @Test
    @Transactional
    void putNonExistingSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, searchHistory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(searchHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(searchHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(searchHistory)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSearchHistoryWithPatch() throws Exception {
        // Initialize the database
        searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the searchHistory using partial update
        SearchHistory partialUpdatedSearchHistory = new SearchHistory();
        partialUpdatedSearchHistory.setId(searchHistory.getId());

        partialUpdatedSearchHistory.searchedDate(UPDATED_SEARCHED_DATE);

        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSearchHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSearchHistory))
            )
            .andExpect(status().isOk());

        // Validate the SearchHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        /*assertSearchHistoryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSearchHistory, searchHistory),
            getPersistedSearchHistory(searchHistory)
        );*/
    }

    @Test
    @Transactional
    void fullUpdateSearchHistoryWithPatch() throws Exception {
        // Initialize the database
        searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the searchHistory using partial update
        SearchHistory partialUpdatedSearchHistory = new SearchHistory();
        partialUpdatedSearchHistory.setId(searchHistory.getId());

        partialUpdatedSearchHistory.searchedDate(UPDATED_SEARCHED_DATE).dayOfWeek(UPDATED_DAY_OF_WEEK);

        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSearchHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSearchHistory))
            )
            .andExpect(status().isOk());

        // Validate the SearchHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSearchHistoryUpdatableFieldsEquals(partialUpdatedSearchHistory, getPersistedSearchHistory(partialUpdatedSearchHistory));
    }

    @Test
    @Transactional
    void patchNonExistingSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, searchHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(searchHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(searchHistory))
            )
            .andExpect(status().isBadRequest());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSearchHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        searchHistory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSearchHistoryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(searchHistory)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SearchHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSearchHistory() throws Exception {
        // Initialize the database
        searchHistoryRepository.saveAndFlush(searchHistory);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the searchHistory
        restSearchHistoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, searchHistory.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return searchHistoryRepository.count();
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

    protected SearchHistory getPersistedSearchHistory(SearchHistory searchHistory) {
        return searchHistoryRepository.findById(searchHistory.getId()).orElseThrow();
    }

    protected void assertPersistedSearchHistoryToMatchAllProperties(SearchHistory expectedSearchHistory) {
        assertSearchHistoryAllPropertiesEquals(expectedSearchHistory, getPersistedSearchHistory(expectedSearchHistory));
    }

    protected void assertPersistedSearchHistoryToMatchUpdatableProperties(SearchHistory expectedSearchHistory) {
        assertSearchHistoryAllUpdatablePropertiesEquals(expectedSearchHistory, getPersistedSearchHistory(expectedSearchHistory));
    }
}
