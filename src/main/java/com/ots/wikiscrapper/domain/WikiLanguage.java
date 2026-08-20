package com.ots.wikiscrapper.domain;

/** Wikipedia content language for fetches and UI display. */
public enum WikiLanguage {
    En,
    Pl;

    public static WikiLanguage parse(String value) {
        if (value != null && value.equalsIgnoreCase("pl")) {
            return Pl;
        }
        return En;
    }

    public String toCode() {
        return this == Pl ? "pl" : "en";
    }

    public String toDisplayName() {
        return this == Pl ? "Polski" : "English";
    }
}
