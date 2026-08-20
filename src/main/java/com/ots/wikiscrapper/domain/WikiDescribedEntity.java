package com.ots.wikiscrapper.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

/** Shared English and Polish Wikipedia summary fields for {@link Country} and {@link Voivodeship}. */
@MappedSuperclass
public abstract class WikiDescribedEntity extends BaseEntity {

    @Column(length = 500)
    private String wikiUrl;

    @Lob
    private String description;

    private Instant fetchedAt;

    @Column(length = 500)
    private String wikiUrlPl;

    @Lob
    private String descriptionPl;

    private Instant fetchedAtPl;

    public String getWikiUrl() {
        return wikiUrl;
    }

    public String getDescription() {
        return description;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public String getWikiUrlPl() {
        return wikiUrlPl;
    }

    public String getDescriptionPl() {
        return descriptionPl;
    }

    public Instant getFetchedAtPl() {
        return fetchedAtPl;
    }

    public String getDescription(WikiLanguage language) {
        return language == WikiLanguage.Pl ? descriptionPl : description;
    }

    public String getWikiUrl(WikiLanguage language) {
        return language == WikiLanguage.Pl ? wikiUrlPl : wikiUrl;
    }

    public Instant getFetchedAt(WikiLanguage language) {
        return language == WikiLanguage.Pl ? fetchedAtPl : fetchedAt;
    }

    public boolean isFetched(WikiLanguage language) {
        return getDescription(language) != null;
    }

    public void applySummary(WikiPageSummary summary, WikiLanguage language) {
        if (language == WikiLanguage.Pl) {
            this.descriptionPl = summary.extract();
            this.wikiUrlPl = summary.pageUrl();
            this.fetchedAtPl = Instant.now();
            return;
        }
        this.description = summary.extract();
        this.wikiUrl = summary.pageUrl();
        this.fetchedAt = Instant.now();
    }
}
