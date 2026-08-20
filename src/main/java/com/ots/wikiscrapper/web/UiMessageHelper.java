package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.domain.WikiLanguage;

/** Request-scoped helper for parameterized UI strings in Thymeleaf. */
public record UiMessageHelper(UiMessageCatalog catalog, WikiLanguage language) {

    public String t(String key, Object... args) {
        return catalog.get(language, key, args);
    }
}
