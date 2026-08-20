package com.ots.wikiscrapper.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** One of the 16 Polish voivodeships (provinces), with an optional Wikipedia-sourced description. */
@Entity
@Table(name = "Voivodeships")
public class Voivodeship extends WikiDescribedEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String wikiTitle;

    @Column(length = 200)
    private String wikiTitlePl;

    protected Voivodeship() {
    }

    public Voivodeship(String name, String wikiTitle) {
        this.name = name;
        this.wikiTitle = wikiTitle;
        this.wikiTitlePl = name;
    }

    public String getName() {
        return name;
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
