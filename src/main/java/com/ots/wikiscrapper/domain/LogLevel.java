package com.ots.wikiscrapper.domain;

/** Severity of an {@link AppLog} audit entry. */
public enum LogLevel {
    Information,
    Warning,
    Error;

    /** Bootstrap badge CSS class for the logs and dashboard views. */
    public String getBadgeClass() {
        return switch (this) {
            case Error -> "bg-danger";
            case Warning -> "bg-warning text-dark";
            default -> "bg-secondary";
        };
    }
}
