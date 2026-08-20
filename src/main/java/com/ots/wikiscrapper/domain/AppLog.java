package com.ots.wikiscrapper.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Persistent audit/log entry written during synchronisation to record progress, warnings, and errors. */
@Entity
@Table(name = "AppLogs")
public class AppLog extends BaseEntity {

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogLevel level;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, length = 100)
    private String source;

    @Lob
    private String exception;

    protected AppLog() {
    }

    public AppLog(LogLevel level, String message, String source, String exception) {
        this.level = level;
        this.message = message;
        this.source = source;
        this.exception = exception;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    public String getException() {
        return exception;
    }

    public String getCreatedAtLocal() {
        return LOCAL.format(getCreatedAt().atZone(ZoneId.systemDefault()));
    }

    public String getLevelBadgeClass() {
        return level.getBadgeClass();
    }
}
