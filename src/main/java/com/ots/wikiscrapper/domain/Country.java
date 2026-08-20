package com.ots.wikiscrapper.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A world country whose Wikipedia summary can be fetched and persisted during synchronisation. */
@Entity
@Table(name = "Countries")
public class Country extends WikiDescribedEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 2)
    private String code;

    @Column(nullable = false, length = 200)
    private String wikiTitle;

    @Column(length = 200)
    private String wikiTitlePl;

    protected Country() {
    }

    public Country(String name, String code, String wikiTitle) {
        this.name = name;
        this.code = code;
        this.wikiTitle = wikiTitle;
        this.wikiTitlePl = PolishCountryWikiTitles.get(code);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getWikiTitle() {
        return wikiTitle;
    }

    public String getWikiTitlePl() {
        return wikiTitlePl;
    }

    public String getWikiTitle(WikiLanguage language) {
        return language == WikiLanguage.Pl ? wikiTitlePl : wikiTitle;
    }

    public void setWikiTitlePl(String wikiTitlePl) {
        this.wikiTitlePl = wikiTitlePl;
    }
}
