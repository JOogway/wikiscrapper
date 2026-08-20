package com.ots.wikiscrapper.service;

import com.ots.wikiscrapper.config.WikipediaProperties;
import com.ots.wikiscrapper.data.AppLogRepository;
import com.ots.wikiscrapper.data.CountryRepository;
import com.ots.wikiscrapper.data.VoivodeshipRepository;
import com.ots.wikiscrapper.domain.AppLog;
import com.ots.wikiscrapper.domain.Country;
import com.ots.wikiscrapper.domain.LogLevel;
import com.ots.wikiscrapper.domain.SyncResult;
import com.ots.wikiscrapper.domain.Voivodeship;
import com.ots.wikiscrapper.domain.WikiDescribedEntity;
import com.ots.wikiscrapper.domain.WikiLanguage;
import com.ots.wikiscrapper.domain.WikiPageSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Orchestrates a full synchronisation: fetches Wikipedia summaries for every voivodeship and country
 * using virtual-thread concurrency (bounded by a semaphore), persists results in batched
 * transactions of {@link #SAVE_BATCH_SIZE} saves, and reports progress via {@link SyncJobService}.
 * Each item is independently retried on rate-limit errors with exponential back-off.
 */
@Service
public class DataSyncService {

    private static final String LOG_SOURCE = "DataSync";
    static final int SAVE_BATCH_SIZE = 25;
    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);

    private final VoivodeshipRepository voivodeshipRepository;
    private final CountryRepository countryRepository;
    private final WikipediaService wikipediaService;
    private final AppLogRepository appLogRepository;
    private final SyncJobService progress;
    private final TransactionTemplate transactions;
    private final int maxConcurrency;
    private final Duration rateLimitRetryDelay;
    private final int rateLimitRetryAttempts;
    private final Object dbLock = new Object();
    private final List<Runnable> pendingSaves = new ArrayList<>();

    @Autowired
    public DataSyncService(
            VoivodeshipRepository voivodeshipRepository,
            CountryRepository countryRepository,
            WikipediaService wikipediaService,
            AppLogRepository appLogRepository,
            SyncJobService progress,
            PlatformTransactionManager transactionManager,
            WikipediaProperties wikipediaProperties) {
        this(voivodeshipRepository, countryRepository, wikipediaService, appLogRepository, progress,
                new TransactionTemplate(transactionManager), wikipediaProperties.maxConcurrency(), Duration.ofSeconds(4), 3);
    }

    static DataSyncService forTests(
            VoivodeshipRepository voivodeshipRepository,
            CountryRepository countryRepository,
            WikipediaService wikipediaService,
            AppLogRepository appLogRepository,
            SyncJobService progress,
            TransactionTemplate transactions,
            int maxConcurrency,
            Duration rateLimitRetryDelay,
            int rateLimitRetryAttempts) {
        return new DataSyncService(voivodeshipRepository, countryRepository, wikipediaService, appLogRepository, progress,
                transactions, maxConcurrency, rateLimitRetryDelay, rateLimitRetryAttempts);
    }

    private DataSyncService(
            VoivodeshipRepository voivodeshipRepository,
            CountryRepository countryRepository,
            WikipediaService wikipediaService,
            AppLogRepository appLogRepository,
            SyncJobService progress,
            TransactionTemplate transactions,
            int maxConcurrency,
            Duration rateLimitRetryDelay,
            int rateLimitRetryAttempts) {
        this.voivodeshipRepository = voivodeshipRepository;
        this.countryRepository = countryRepository;
        this.wikipediaService = wikipediaService;
        this.appLogRepository = appLogRepository;
        this.progress = progress;
        this.transactions = transactions;
        this.maxConcurrency = Math.min(Math.max(maxConcurrency, 1), 32);
        this.rateLimitRetryDelay = rateLimitRetryDelay;
        this.rateLimitRetryAttempts = Math.min(Math.max(rateLimitRetryAttempts, 1), 6);
    }

    public SyncResult syncAll() {
        Instant startedAt = Instant.now();
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();

        log.info("Data synchronization started with max concurrency {}", maxConcurrency);
        audit(LogLevel.Information, "Data synchronization started", null);

        List<Voivodeship> voivodeships = voivodeshipRepository.findAllByOrderByNameAsc();
        List<Country> countries = countryRepository.findAllByOrderByNameAsc();
        List<WorkItem> workItems = buildWorkItems(voivodeships, countries);
        progress.begin(workItems.size());

        Semaphore limiter = new Semaphore(maxConcurrency);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = workItems.stream()
                    .map(item -> CompletableFuture.runAsync(() -> {
                        try {
                            limiter.acquire();
                            progress.itemStarted(item.name);
                            Outcome outcome = syncItem(item, errors);
                            tally(outcome, succeeded, failed, skipped);
                            progress.itemFinished(outcome == Outcome.SUCCEEDED, outcome == Outcome.SKIPPED);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(e);
                        } finally {
                            limiter.release();
                        }
                    }, executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }

        // Persist whatever the last partial batch still holds before reporting completion.
        flushPendingSaves();

        SyncResult result = new SyncResult(
                succeeded.get(),
                failed.get(),
                skipped.get(),
                List.copyOf(errors),
                startedAt,
                Instant.now());

        log.info("Data synchronization finished: {} succeeded, {} failed, {} skipped",
                result.succeeded(), result.failed(), result.skipped());
        audit(
                result.failed() > 0 ? LogLevel.Warning : LogLevel.Information,
                "Data synchronization finished: %d succeeded, %d failed, %d skipped."
                        .formatted(result.succeeded(), result.failed(), result.skipped()),
                null);
        progress.complete(result);
        return result;
    }

    private List<WorkItem> buildWorkItems(List<Voivodeship> voivodeships, List<Country> countries) {
        List<WorkItem> items = new ArrayList<>((voivodeships.size() + countries.size()) * 2);
        addWorkItems(items, voivodeships, WikiLanguage.En, Voivodeship::getName, Voivodeship::getWikiTitle, voivodeshipRepository::save);
        addWorkItems(items, voivodeships, WikiLanguage.Pl, Voivodeship::getName, Voivodeship::getWikiTitlePl, voivodeshipRepository::save);
        addWorkItems(items, countries, WikiLanguage.En, Country::getName, Country::getWikiTitle, countryRepository::save);
        addWorkItems(items, countries, WikiLanguage.Pl, Country::getName, Country::getWikiTitlePl, countryRepository::save);
        return items;
    }

    private <T extends WikiDescribedEntity> void addWorkItems(
            List<WorkItem> items,
            List<T> entities,
            WikiLanguage language,
            Function<T, String> displayName,
            Function<T, String> wikiTitle,
            Consumer<T> save) {
        for (T entity : entities) {
            items.add(new WorkItem(
                    displayName.apply(entity),
                    wikiTitle.apply(entity),
                    language,
                    summary -> {
                        entity.applySummary(summary, language);
                        queueSave(() -> save.accept(entity));
                    }));
        }
    }

    private Outcome syncItem(WorkItem item, ConcurrentLinkedQueue<String> errors) {
        Exception lastRateLimit = null;
        for (int attempt = 1; attempt <= rateLimitRetryAttempts; attempt++) {
            try {
                var summary = wikipediaService.getPageSummary(item.wikiTitle, item.language);
                if (summary.isEmpty()) {
                    String message = "No Wikipedia summary available for '%s' (%s, page: '%s')."
                            .formatted(item.name, item.language, item.wikiTitle);
                    errors.add(message);
                    log.warn("Skipped {}: no summary for page {}", item.name, item.wikiTitle);
                    audit(LogLevel.Warning, message, null);
                    return Outcome.SKIPPED;
                }
                item.persist.accept(summary.get());
                log.debug("Synced {} from page {}", item.name, item.wikiTitle);
                return Outcome.SUCCEEDED;
            } catch (Exception ex) {
                if (WikipediaResilience.isRateLimited(ex) && attempt < rateLimitRetryAttempts) {
                    lastRateLimit = ex;
                    Duration wait = resolveItemRetryDelay(attempt);
                    log.warn("Rate-limited while syncing {} (page {}); waiting {} before retry {}/{}",
                            item.name, item.wikiTitle, wait, attempt, rateLimitRetryAttempts);
                    sleep(wait);
                    continue;
                }
                return failItem(item, errors, ex);
            }
        }
        return failItem(item, errors, lastRateLimit);
    }

    private Duration resolveItemRetryDelay(int attempt) {
        if (rateLimitRetryDelay.isZero() || rateLimitRetryDelay.isNegative()) {
            return Duration.ZERO;
        }
        Duration delay = Duration.ofMillis(
                (long) (rateLimitRetryDelay.toMillis() * Math.pow(2, attempt - 1)));
        return delay.compareTo(WikipediaResilience.MAX_RETRY_DELAY) > 0
                ? WikipediaResilience.MAX_RETRY_DELAY
                : delay;
    }

    private Outcome failItem(WorkItem item, ConcurrentLinkedQueue<String> errors, Exception ex) {
        String message = "Failed to sync '%s' (page: '%s'): %s"
                .formatted(item.name, item.wikiTitle, ex.getMessage());
        errors.add(message);
        log.error("Failed to sync {} from page {}", item.name, item.wikiTitle, ex);
        audit(LogLevel.Error, message, stackTrace(ex));
        return Outcome.FAILED;
    }

    private void persist(Runnable action) {
        synchronized (dbLock) {
            transactions.executeWithoutResult(status -> action.run());
        }
    }

    /**
     * Buffers an entity save and commits the buffer as a single transaction once
     * {@link #SAVE_BATCH_SIZE} saves have accumulated. On failure the buffer is kept,
     * so the pending saves are retried with the next flush instead of being lost.
     */
    private void queueSave(Runnable save) {
        synchronized (dbLock) {
            pendingSaves.add(save);
            if (pendingSaves.size() >= SAVE_BATCH_SIZE) {
                flushPendingSavesLocked();
            }
        }
    }

    private void flushPendingSaves() {
        synchronized (dbLock) {
            flushPendingSavesLocked();
        }
    }

    private void flushPendingSavesLocked() {
        if (pendingSaves.isEmpty()) {
            return;
        }
        transactions.executeWithoutResult(status -> pendingSaves.forEach(Runnable::run));
        pendingSaves.clear();
    }

    private void audit(LogLevel level, String message, String exception) {
        try {
            persist(() -> appLogRepository.save(new AppLog(level, message, LOG_SOURCE, exception)));
        } catch (Exception ex) {
            log.error("Failed to write audit entry: {}", message, ex);
        }
    }

    private static void tally(Outcome outcome, AtomicInteger succeeded, AtomicInteger failed, AtomicInteger skipped) {
        switch (outcome) {
            case SUCCEEDED -> succeeded.incrementAndGet();
            case FAILED -> failed.incrementAndGet();
            case SKIPPED -> skipped.incrementAndGet();
        }
    }

    private static String stackTrace(Exception ex) {
        java.io.StringWriter writer = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(writer));
        return writer.toString();
    }

    private static void sleep(Duration wait) {
        if (wait.isZero()) {
            return;
        }
        try {
            Thread.sleep(wait.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private enum Outcome { SUCCEEDED, FAILED, SKIPPED }

    private record WorkItem(String name, String wikiTitle, WikiLanguage language, Consumer<WikiPageSummary> persist) {
    }
}
