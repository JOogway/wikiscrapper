package com.ots.wikiscrapper.domain;

/** Immutable snapshot of a Wikipedia page summary as returned by the REST API. */
public record WikiPageSummary(String title, String extract, String pageUrl) {
}
