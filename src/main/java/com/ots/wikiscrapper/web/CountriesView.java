package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.domain.CountryListSort;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/** Thymeleaf view-model helper that builds pagination and sort URLs for the countries table, keeping query state intact. */
public record CountriesView(
        String search,
        String fetched,
        String sort,
        String dir,
        String pageSize
) {
    public String pageUrl(int page) {
        return "/countries?" + query(page, sort, dir);
    }

    public String sortUrl(String column) {
        return "/countries?" + query(1, CountryListSort.normalizeColumn(column),
                CountryListSort.nextDirection(sort, dir, column));
    }

    public String ariaSort(String column) {
        return CountryListSort.ariaSort(sort, dir, column);
    }

    public String sortMarker(String column) {
        return CountryListSort.sortMarker(sort, dir, column);
    }

    private String query(int page, String sortColumn, String sortDir) {
        StringJoiner joiner = new StringJoiner("&");
        joiner.add("page=" + page);
        joiner.add("pageSize=" + enc(pageSize));
        joiner.add("sort=" + enc(sortColumn));
        joiner.add("dir=" + enc(sortDir));
        if (search != null && !search.isBlank()) {
            joiner.add("search=" + enc(search));
        }
        if (fetched != null && !fetched.isBlank()) {
            joiner.add("fetched=" + enc(fetched));
        }
        return joiner.toString();
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
