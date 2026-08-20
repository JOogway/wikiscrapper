package com.ots.wikiscrapper.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/** Real-time progress snapshot of the background sync job, exposed via the REST API and polled by the UI. */
public record SyncStatusDto(
        @JsonProperty("isRunning") boolean isRunning,
        int processed,
        int total,
        int succeeded,
        int failed,
        int skipped,
        String currentItem,
        Instant startedAtUtc,
        Instant completedAtUtc,
        List<String> errors
) {
    @JsonProperty("percent")
    public Double percent() {
        return total <= 0 ? null : Math.round(1000.0 * processed / total) / 10.0;
    }
}
