package com.ots.wikiscrapper.service;

import com.ots.wikiscrapper.config.WikipediaClientConfig;
import com.ots.wikiscrapper.domain.WikiLanguage;
import com.ots.wikiscrapper.domain.WikiPageSummary;
import com.ots.wikiscrapper.service.dto.WikipediaSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/** Thin client for the English and Polish Wikipedia REST v1 summary endpoints. */
@Service
public class WikipediaService {

    private static final Logger log = LoggerFactory.getLogger(WikipediaService.class);
    static final int MAX_RETRY_ATTEMPTS = 5;
    static final Duration MAX_RETRY_DELAY = WikipediaResilience.MAX_RETRY_DELAY;

    private final Map<WikiLanguage, RestClient> restClients;

    @Autowired
    public WikipediaService(
            @Qualifier(WikipediaClientConfig.EN_CLIENT) RestClient enClient,
            @Qualifier(WikipediaClientConfig.PL_CLIENT) RestClient plClient) {
        this.restClients = Map.of(WikiLanguage.En, enClient, WikiLanguage.Pl, plClient);
    }

    public Optional<WikiPageSummary> getPageSummary(String pageTitle) {
        return getPageSummary(pageTitle, WikiLanguage.En, true);
    }

    public Optional<WikiPageSummary> getPageSummary(String pageTitle, WikiLanguage language) {
        return getPageSummary(pageTitle, language, true);
    }

    Optional<WikiPageSummary> getPageSummary(String pageTitle, boolean retryOn429) {
        return getPageSummary(pageTitle, WikiLanguage.En, retryOn429);
    }

    Optional<WikiPageSummary> getPageSummary(String pageTitle, WikiLanguage language, boolean retryOn429) {
        if (pageTitle == null || pageTitle.isBlank()) {
            throw new IllegalArgumentException("pageTitle must not be blank");
        }

        RestClient restClient = restClients.get(language);
        String titlePath = pageTitle.replace(' ', '_');
        int attempts = retryOn429 ? MAX_RETRY_ATTEMPTS : 1;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                ResponseEntity<WikipediaSummaryResponse> response = restClient.get()
                        .uri("/page/summary/{title}?redirect=true", titlePath)
                        .retrieve()
                        .toEntity(WikipediaSummaryResponse.class);

                WikipediaSummaryResponse body = response.getBody();
                if (body == null) {
                    log.warn("Wikipedia returned an empty body for {} ({})", pageTitle, language);
                    return Optional.empty();
                }

                String extract = body.extract();
                if (extract == null || extract.isBlank()) {
                    log.warn("Wikipedia returned an empty summary for {} ({})", pageTitle, language);
                    return Optional.empty();
                }

                String title = body.title() != null ? body.title() : pageTitle;
                return Optional.of(new WikiPageSummary(title, extract.trim(), body.pageUrl()));
            } catch (RestClientResponseException ex) {
                if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                    log.warn("Wikipedia page not found ({}) : {}", language, pageTitle);
                    return Optional.empty();
                }
                if (retryOn429 && ex.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS) && attempt < attempts) {
                    Duration wait = resolveRetryDelay(ex, attempt);
                    log.warn("Wikipedia 429 for {} ({}) — waiting {} before attempt {}/{}",
                            pageTitle, language, wait, attempt, attempts);
                    sleep(wait);
                    continue;
                }
                throw ex;
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    static Duration resolveRetryDelay(RestClientResponseException ex, int attempt) {
        return WikipediaResilience.resolveRetryDelay(ex, attempt);
    }

    private static void sleep(Duration wait) {
        try {
            Thread.sleep(wait.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry Wikipedia", e);
        }
    }
}
