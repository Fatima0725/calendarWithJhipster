package com.groupeisi.web.rest;

import com.groupeisi.domain.SearchHistory;
import com.groupeisi.repository.SearchHistoryRepository;
import com.groupeisi.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.groupeisi.domain.SearchHistory}.
 */
@RestController
@RequestMapping("/api/search-histories")
@Transactional
public class SearchHistoryResource {

    private final Logger log = LoggerFactory.getLogger(SearchHistoryResource.class);

    private static final String ENTITY_NAME = "searchHistory";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SearchHistoryRepository searchHistoryRepository;

    public SearchHistoryResource(SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    /**
     * {@code POST  /search-histories} : Create a new searchHistory.
     *
     * @param searchHistory the searchHistory to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new searchHistory, or with status {@code 400 (Bad Request)} if the searchHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SearchHistory> createSearchHistory(@RequestBody SearchHistory searchHistory) throws URISyntaxException {
        log.debug("REST request to save SearchHistory : {}", searchHistory);
        if (searchHistory.getId() != null) {
            throw new BadRequestAlertException("A new searchHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        searchHistory = searchHistoryRepository.save(searchHistory);
        return ResponseEntity.created(new URI("/api/search-histories/" + searchHistory.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, searchHistory.getId().toString()))
            .body(searchHistory);
    }

    /**
     * {@code PUT  /search-histories/:id} : Updates an existing searchHistory.
     *
     * @param id the id of the searchHistory to save.
     * @param searchHistory the searchHistory to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated searchHistory,
     * or with status {@code 400 (Bad Request)} if the searchHistory is not valid,
     * or with status {@code 500 (Internal Server Error)} if the searchHistory couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SearchHistory> updateSearchHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SearchHistory searchHistory
    ) throws URISyntaxException {
        log.debug("REST request to update SearchHistory : {}, {}", id, searchHistory);
        if (searchHistory.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, searchHistory.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!searchHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        searchHistory = searchHistoryRepository.save(searchHistory);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, searchHistory.getId().toString()))
            .body(searchHistory);
    }

    /**
     * {@code PATCH  /search-histories/:id} : Partial updates given fields of an existing searchHistory, field will ignore if it is null
     *
     * @param id the id of the searchHistory to save.
     * @param searchHistory the searchHistory to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated searchHistory,
     * or with status {@code 400 (Bad Request)} if the searchHistory is not valid,
     * or with status {@code 404 (Not Found)} if the searchHistory is not found,
     * or with status {@code 500 (Internal Server Error)} if the searchHistory couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SearchHistory> partialUpdateSearchHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SearchHistory searchHistory
    ) throws URISyntaxException {
        log.debug("REST request to partial update SearchHistory partially : {}, {}", id, searchHistory);
        if (searchHistory.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, searchHistory.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!searchHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SearchHistory> result = searchHistoryRepository
            .findById(searchHistory.getId())
            .map(existingSearchHistory -> {
                if (searchHistory.getSearchedDate() != null) {
                    existingSearchHistory.setSearchedDate(searchHistory.getSearchedDate());
                }
                if (searchHistory.getDayOfWeek() != null) {
                    existingSearchHistory.setDayOfWeek(searchHistory.getDayOfWeek());
                }

                return existingSearchHistory;
            })
            .map(searchHistoryRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, searchHistory.getId().toString())
        );
    }

    /**
     * {@code GET  /search-histories} : get all the searchHistories.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of searchHistories in body.
     */
    @GetMapping("")
    public List<SearchHistory> getAllSearchHistories() {
        log.debug("REST request to get all SearchHistories");
        return searchHistoryRepository.findAll();
    }

    /**
     * {@code GET  /search-histories/:id} : get the "id" searchHistory.
     *
     * @param id the id of the searchHistory to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the searchHistory, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SearchHistory> getSearchHistory(@PathVariable("id") Long id) {
        log.debug("REST request to get SearchHistory : {}", id);
        Optional<SearchHistory> searchHistory = searchHistoryRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(searchHistory);
    }

    /**
     * {@code DELETE  /search-histories/:id} : delete the "id" searchHistory.
     *
     * @param id the id of the searchHistory to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSearchHistory(@PathVariable("id") Long id) {
        log.debug("REST request to delete SearchHistory : {}", id);
        searchHistoryRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
