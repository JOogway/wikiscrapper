package com.ots.wikiscrapper.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

/** Shared retry and rate-limit helpers for Wikipedia HTTP clients. */
public final class WikipediaResilience {

    public static final Duration MAX_RETRY_DELAY = Duration.ofSeconds(20);

    private WikipediaResilience() {
    }

    /** Derives the retry delay from Retry-After when present, otherwise exponential back-off. */
    public static Duration resolveRetryDelay(RestClientResponseException ex, int attempt) {
        String retryAfter = ex.getResponseHeaders() != null
                ? ex.getResponseHeaders().getFirst("Retry-After")
                : null;
        if (retryAfter != null) {
            try {
                Duration fromHeader = Duration.ofSeconds(Long.parseLong(retryAfter.trim()));
                if (!fromHeader.isNegative() && !fromHeader.isZero()) {
                    return cap(fromHeader);
                }
            } catch (NumberFormatException ignored) {
                // HTTP-date Retry-After is rare here; fall through to exponential backoff.
            }
        }
        Duration exponential = Duration.ofSeconds((long) (2 * Math.pow(2, attempt - 1)));
        return cap(exponential);
    }

    /** Walks the exception cause chain to detect a 429 Too Many Requests response. */
    public static boolean isRateLimited(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current instanceof RestClientResponseException rest
                    && rest.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.contains("429") && message.toLowerCase().contains("too many requests")) {
                return true;
            }
        }
        return false;
    }

    static Duration cap(Duration delay) {
        return delay.compareTo(MAX_RETRY_DELAY) > 0 ? MAX_RETRY_DELAY : delay;
    }
}
