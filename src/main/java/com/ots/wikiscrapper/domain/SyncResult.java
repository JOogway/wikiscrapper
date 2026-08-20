package com.ots.wikiscrapper.domain;

import java.time.Instant;
import java.util.List;

/** Final outcome of a completed data-sync run, including per-item tallies and error messages. */
public record SyncResult(
        int succeeded,
        int failed,
        int skipped,
        List<String> errors,
        Instant startedAtUtc,
        Instant completedAtUtc
) {
    public int total() {
        return succeeded + failed + skipped;
    }
}
