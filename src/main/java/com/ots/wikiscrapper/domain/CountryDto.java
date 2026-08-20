package com.ots.wikiscrapper.domain;

import java.time.Instant;

/** Language-resolved representation of a {@link Country} for the API and list views. */
public record CountryDto(
        Integer id,
        String name,
        String code,
        String wikiTitle,
        String wikiUrl,
        String description,
        Instant fetchedAt
) {
    public static CountryDto from(Country entity, WikiLanguage language) {
        return new CountryDto(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getWikiTitle(language),
                entity.getWikiUrl(language),
                entity.getDescription(language),
                entity.getFetchedAt(language));
    }

    /** Whether a description has been fetched in the resolved language. */
    public boolean isFetched() {
        return description != null;
    }

    /** Description truncated for table cells. */
    public String descriptionPreview(int max) {
        return WikiFormat.preview(description, max);
    }

    /** Fetch timestamp formatted in the server's local time zone. */
    public String fetchedAtLocal() {
        return WikiFormat.formatLocal(fetchedAt);
    }
}
