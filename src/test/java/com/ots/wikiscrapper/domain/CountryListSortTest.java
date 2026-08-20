package com.ots.wikiscrapper.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountryListSortTest {

    @Test
    void unknownColumnFallsBackToName() {
        assertThat(CountryListSort.normalizeColumn("nope")).isEqualTo(CountryListSort.NAME);
        assertThat(CountryListSort.normalizeColumn(null)).isEqualTo(CountryListSort.NAME);
    }

    @Test
    void nextDirectionTogglesActiveColumn() {
        assertThat(CountryListSort.nextDirection("name", "asc", "name")).isEqualTo("desc");
        assertThat(CountryListSort.nextDirection("name", "desc", "name")).isEqualTo("asc");
    }

    @Test
    void fetchedStartsDescending() {
        assertThat(CountryListSort.nextDirection("name", "asc", "fetched")).isEqualTo("desc");
    }
}
