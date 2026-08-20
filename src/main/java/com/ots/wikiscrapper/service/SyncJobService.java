package com.ots.wikiscrapper.service;

import com.ots.wikiscrapper.domain.SyncResult;
import com.ots.wikiscrapper.domain.SyncStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the lifecycle of a single background sync job. Ensures at most one sync runs at a time
 * (via CAS on an {@link AtomicBoolean}) and exposes thread-safe progress state that the UI and
 * REST API poll for real-time feedback.
 */
@Service
public class SyncJobService {

    private static final Logger log = LoggerFactory.getLogger(SyncJobService.class);

    private final DataSyncService dataSyncService;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Object gate = new Object();

    private boolean isRunning;
    private int processed;
    private int total;
    private int succeeded;
    private int failed;
    private int skipped;
    private String currentItem;
    private Instant startedAtUtc;
    private Instant completedAtUtc;
    private List<String> errors = List.of();

    public SyncJobService(@Lazy DataSyncService dataSyncService) {
        this.dataSyncService = dataSyncService;
    }

    public boolean tryStart() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        synchronized (gate) {
            isRunning = true;
            processed = 0;
            total = 0;
            succeeded = 0;
            failed = 0;
            skipped = 0;
            currentItem = null;
            startedAtUtc = Instant.now();
            completedAtUtc = null;
            errors = new ArrayList<>();
        }
        executor.execute(this::run);
        return true;
    }

    public SyncStatusDto getStatus() {
        synchronized (gate) {
            return new SyncStatusDto(
                    isRunning || running.get(),
                    processed,
                    total,
                    succeeded,
                    failed,
                    skipped,
                    currentItem,
                    startedAtUtc,
                    completedAtUtc,
                    List.copyOf(errors));
        }
    }

    public void begin(int itemTotal) {
        synchronized (gate) {
            total = itemTotal;
        }
    }

    public void itemStarted(String itemName) {
        synchronized (gate) {
            currentItem = itemName;
        }
    }

    public void itemFinished(boolean itemSucceeded, boolean itemSkipped) {
        synchronized (gate) {
            processed++;
            if (itemSucceeded) {
                succeeded++;
            } else if (itemSkipped) {
                skipped++;
            } else {
                failed++;
            }
        }
    }

    public void complete(SyncResult result) {
        synchronized (gate) {
            isRunning = false;
            currentItem = null;
            succeeded = result.succeeded();
            failed = result.failed();
            skipped = result.skipped();
            processed = result.total();
            total = Math.max(total, result.total());
            errors = new ArrayList<>(result.errors());
            completedAtUtc = result.completedAtUtc();
        }
    }

    private void run() {
        try {
            dataSyncService.syncAll();
        } catch (Exception ex) {
            log.error("Background synchronization failed", ex);
            synchronized (gate) {
                isRunning = false;
                currentItem = null;
                completedAtUtc = Instant.now();
                errors = new ArrayList<>(errors);
                errors.add("Synchronization aborted: " + ex.getMessage());
            }
        } finally {
            synchronized (gate) {
                isRunning = false;
                currentItem = null;
            }
            running.set(false);
        }
    }
}
