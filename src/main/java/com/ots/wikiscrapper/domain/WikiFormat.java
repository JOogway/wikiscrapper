package com.ots.wikiscrapper.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Shared display formatting for Wikipedia summary data (timestamps and text previews). */
public final class WikiFormat {

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private WikiFormat() {
    }

    /** Formats a UTC instant in the server's local time zone, or returns null. */
    public static String formatLocal(Instant instant) {
        return instant == null ? null : LOCAL.format(instant.atZone(ZoneId.systemDefault()));
    }

    /** Truncates text to {@code max} characters with an ellipsis, or returns null. */
    public static String preview(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() > max ? text.substring(0, max) + "…" : text;
    }
}
