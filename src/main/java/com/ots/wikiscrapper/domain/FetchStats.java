package com.ots.wikiscrapper.domain;

import java.time.Instant;

/**
 * Aggregate fetch statistics for one entity type in one Wikipedia language,
 * produced by a single database query.
 *
 * @param total         total number of rows
 * @param fetched       rows that already have a description in the requested language
 * @param lastFetchedAt most recent fetch timestamp in the requested language, or null when none
 */
public record FetchStats(long total, long fetched, Instant lastFetchedAt) {
}
