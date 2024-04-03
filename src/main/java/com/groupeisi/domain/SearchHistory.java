package com.groupeisi.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A SearchHistory.
 */
@Entity
@Table(name = "search_history")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "searched_date")
    private LocalDate searchedDate;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SearchHistory id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSearchedDate() {
        return this.searchedDate;
    }

    public SearchHistory searchedDate(LocalDate searchedDate) {
        this.setSearchedDate(searchedDate);
        return this;
    }

    public void setSearchedDate(LocalDate searchedDate) {
        this.searchedDate = searchedDate;
    }

    public String getDayOfWeek() {
        return this.dayOfWeek;
    }

    public SearchHistory dayOfWeek(String dayOfWeek) {
        this.setDayOfWeek(dayOfWeek);
        return this;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchHistory)) {
            return false;
        }
        return getId() != null && getId().equals(((SearchHistory) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SearchHistory{" +
            "id=" + getId() +
            ", searchedDate='" + getSearchedDate() + "'" +
            ", dayOfWeek='" + getDayOfWeek() + "'" +
            "}";
    }
}
