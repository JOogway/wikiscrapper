package com.ots.wikiscrapper.service;

import com.ots.wikiscrapper.domain.WikiPageSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WikipediaServiceTest {

    private static final String POLAND_JSON = """
            {
                "title": "Poland",
                "extract": "Poland is a country in Central Europe.",
                "content_urls": {
                    "desktop": { "page": "https://en.wikipedia.org/wiki/Poland" }
                }
            }
            """;

    private WikipediaService service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://en.wikipedia.org/api/rest_v1/");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new WikipediaService(builder.build(), builder.build());
    }

    @Test
    void returnsSummaryWhenPageExists() {
        server.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Poland?redirect=true"))
                .andRespond(withSuccess(POLAND_JSON, MediaType.APPLICATION_JSON));

        Optional<WikiPageSummary> summary = service.getPageSummary("Poland");

        assertThat(summary).isPresent();
        assertThat(summary.get().title()).isEqualTo("Poland");
        assertThat(summary.get().extract()).isEqualTo("Poland is a country in Central Europe.");
        assertThat(summary.get().pageUrl()).isEqualTo("https://en.wikipedia.org/wiki/Poland");
    }

    @Test
    void encodesSpacesAsUnderscores() {
        server.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Masovian_Voivodeship?redirect=true"))
                .andRespond(withSuccess(POLAND_JSON, MediaType.APPLICATION_JSON));

        service.getPageSummary("Masovian Voivodeship");
        server.verify();
    }

    @Test
    void returnsEmptyWhenPageNotFound() {
        server.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Missing?redirect=true"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(service.getPageSummary("Missing")).isEmpty();
    }

    @Test
    void returnsEmptyWhenExtractIsBlank() {
        server.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Empty?redirect=true"))
                .andRespond(withSuccess("""
                        { "title": "Empty", "extract": "" }
                        """, MediaType.APPLICATION_JSON));

        assertThat(service.getPageSummary("Empty")).isEmpty();
    }

    @Test
    void throwsOnRateLimitingWhenRetriesDisabled() {
        server.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Poland?redirect=true"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> service.getPageSummary("Poland", false))
                .isInstanceOf(RestClientResponseException.class);
    }

    @Test
    void throwsOnBlankTitle() {
        assertThatThrownBy(() -> service.getPageSummary("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void capsLongRetryAfter() {
        RestClientResponseException ex = new RestClientResponseException(
                "429", HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests",
                null, null, null);
        // no Retry-After header -> exponential
        assertThat(WikipediaService.resolveRetryDelay(ex, 1)).isEqualTo(Duration.ofSeconds(2));
        assertThat(WikipediaService.resolveRetryDelay(ex, 5)).isEqualTo(WikipediaService.MAX_RETRY_DELAY);
    }
}
