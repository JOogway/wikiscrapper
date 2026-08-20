package com.ots.wikiscrapper.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Typed Wikipedia REST v1 page summary response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WikipediaSummaryResponse(
        String title,
        String extract,
        @JsonProperty("content_urls") ContentUrls contentUrls) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentUrls(Desktop desktop) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Desktop(String page) {
    }

    public String pageUrl() {
        return contentUrls != null && contentUrls.desktop() != null
                ? contentUrls.desktop().page()
                : null;
    }
}
