package com.ots.wikiscrapper.service;

import com.ots.wikiscrapper.domain.WikiLanguage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/** Cookie-backed Wikipedia language preference for the current HTTP request. */
@Component
@RequestScope
public class WikiLanguageAccessor {

    public static final String COOKIE_NAME = "wiki_lang";

    private final WikiLanguage current;

    public WikiLanguageAccessor(HttpServletRequest request) {
        String queryLang = request.getParameter("lang");
        if (queryLang != null && !queryLang.isBlank()) {
            current = WikiLanguage.parse(queryLang);
            return;
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    current = WikiLanguage.parse(cookie.getValue());
                    return;
                }
            }
        }
        current = WikiLanguage.En;
    }

    public WikiLanguage getCurrent() {
        return current;
    }
}
