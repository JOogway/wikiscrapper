package com.ots.wikiscrapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised configuration for Wikipedia API access, bound from {@code wikipedia.*} properties.
 */
@ConfigurationProperties(prefix = "wikipedia")
public record WikipediaProperties(
        String enBaseUrl,
        String plBaseUrl,
        int maxConcurrency
) {
    public WikipediaProperties {
        if (enBaseUrl == null || enBaseUrl.isBlank()) {
            enBaseUrl = "https://en.wikipedia.org/api/rest_v1/";
        }
        if (plBaseUrl == null || plBaseUrl.isBlank()) {
            plBaseUrl = "https://pl.wikipedia.org/api/rest_v1/";
        }
        if (maxConcurrency < 1) {
            maxConcurrency = 8;
        }
        maxConcurrency = Math.min(maxConcurrency, 32);
    }
}
