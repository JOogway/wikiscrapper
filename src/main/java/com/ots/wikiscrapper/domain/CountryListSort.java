package com.ots.wikiscrapper.domain;

/** Utility constants and helpers for normalising and toggling country-list sort parameters across UI and API. */
public final class CountryListSort {

    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String FETCHED = "fetched";

    private CountryListSort() {
    }

    public static String normalizeColumn(String sort) {
        if (sort == null) {
            return NAME;
        }
        return switch (sort.trim().toLowerCase()) {
            case CODE -> CODE;
            case FETCHED -> FETCHED;
            default -> NAME;
        };
    }

    public static boolean isDescending(String dir) {
        return dir != null && dir.equalsIgnoreCase("desc");
    }

    public static String nextDirection(String currentColumn, String currentDir, String clickedColumn) {
        String current = normalizeColumn(currentColumn);
        String clicked = normalizeColumn(clickedColumn);
        if (!current.equals(clicked)) {
            return FETCHED.equals(clicked) ? "desc" : "asc";
        }
        return isDescending(currentDir) ? "asc" : "desc";
    }

    public static String ariaSort(String sort, String dir, String column) {
        if (!normalizeColumn(sort).equals(normalizeColumn(column))) {
            return "none";
        }
        return isDescending(dir) ? "descending" : "ascending";
    }

    public static String sortMarker(String sort, String dir, String column) {
        return switch (ariaSort(sort, dir, column)) {
            case "ascending" -> " ↑";
            case "descending" -> " ↓";
            default -> "";
        };
    }
}
