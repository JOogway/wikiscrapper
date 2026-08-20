package com.ots.wikiscrapper.service;

import com.ots.wikiscrapper.data.AppLogRepository;
import com.ots.wikiscrapper.data.CountryRepository;
import com.ots.wikiscrapper.data.VoivodeshipRepository;
import com.ots.wikiscrapper.domain.Country;
import com.ots.wikiscrapper.domain.SyncResult;
import com.ots.wikiscrapper.domain.Voivodeship;
import com.ots.wikiscrapper.domain.WikiLanguage;
import com.ots.wikiscrapper.domain.WikiPageSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSyncServiceTest {

    @Mock private VoivodeshipRepository voivodeships;
    @Mock private CountryRepository countries;
    @Mock private WikipediaService wikipedia;
    @Mock private AppLogRepository logs;
    @Mock private SyncJobService progress;
    @Mock private PlatformTransactionManager txManager;
    @Mock private TransactionStatus txStatus;

    private DataSyncService service;

    @BeforeEach
    void setUp() {
        when(txManager.getTransaction(any(TransactionDefinition.class))).thenReturn(txStatus);
        service = DataSyncService.forTests(
                voivodeships, countries, wikipedia, logs, progress,
                new TransactionTemplate(txManager),
                8, Duration.ZERO, 3);
    }

    @Test
    void updatesAllItemsWhenWikipediaSucceeds() {
        Voivodeship voivodeship = new Voivodeship("Województwo mazowieckie", "Masovian Voivodeship");
        Country country = new Country("Poland", "PL", "Poland");
        when(voivodeships.findAllByOrderByNameAsc()).thenReturn(List.of(voivodeship));
        when(countries.findAllByOrderByNameAsc()).thenReturn(List.of(country));
        when(wikipedia.getPageSummary(any(String.class), any(WikiLanguage.class))).thenAnswer(inv -> {
            String title = inv.getArgument(0);
            return Optional.of(new WikiPageSummary(title, title + " description.", "https://en.wikipedia.org/wiki/" + title));
        });

        SyncResult result = service.syncAll();

        assertThat(result.succeeded()).isEqualTo(4);
        assertThat(result.failed()).isZero();
        assertThat(voivodeship.getDescription()).isEqualTo("Masovian Voivodeship description.");
        assertThat(voivodeship.getDescriptionPl()).isEqualTo("Województwo mazowieckie description.");
        assertThat(country.getDescription()).isEqualTo("Poland description.");
        assertThat(country.getDescriptionPl()).isEqualTo("Polska description.");
    }

    @Test
    void skipsWhenWikipediaReturnsEmpty() {
        Country country = new Country("Atlantis", "XX", "Atlantis");
        when(voivodeships.findAllByOrderByNameAsc()).thenReturn(List.of());
        when(countries.findAllByOrderByNameAsc()).thenReturn(List.of(country));
        when(wikipedia.getPageSummary(any(String.class), any(WikiLanguage.class))).thenReturn(Optional.empty());

        SyncResult result = service.syncAll();

        assertThat(result.skipped()).isEqualTo(2);
        assertThat(country.getDescription()).isNull();
        assertThat(country.getDescriptionPl()).isNull();
    }

    @Test
    void retriesRateLimitThenSucceeds() {
        Country country = new Country("Uzbekistan", "UZ", "Uzbekistan");
        when(voivodeships.findAllByOrderByNameAsc()).thenReturn(List.of());
        when(countries.findAllByOrderByNameAsc()).thenReturn(List.of(country));
        RestClientResponseException tooMany = new RestClientResponseException(
                "429 Too Many Requests", HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests",
                null, null, null);
        when(wikipedia.getPageSummary(any(String.class), any(WikiLanguage.class)))
                .thenThrow(tooMany)
                .thenReturn(Optional.of(new WikiPageSummary("Uzbekistan", "Uzbekistan description.", null)));

        SyncResult result = service.syncAll();

        assertThat(result.succeeded()).isEqualTo(2);
        assertThat(country.getDescription()).isEqualTo("Uzbekistan description.");
        assertThat(country.getDescriptionPl()).isEqualTo("Uzbekistan description.");
    }
}
