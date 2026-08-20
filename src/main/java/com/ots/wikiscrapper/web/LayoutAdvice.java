package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.domain.WikiLanguage;
import com.ots.wikiscrapper.service.WikiLanguageAccessor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

/** Exposes layout model attributes for Thymeleaf templates. */
@ControllerAdvice
public class LayoutAdvice {

    private final WikiLanguageAccessor wikiLanguage;
    private final UiMessageCatalog uiMessages;

    public LayoutAdvice(WikiLanguageAccessor wikiLanguage, UiMessageCatalog uiMessages) {
        this.wikiLanguage = wikiLanguage;
        this.uiMessages = uiMessages;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("wikiLang")
    public WikiLanguage wikiLang() {
        return wikiLanguage.getCurrent();
    }

    /** Localized UI strings for the active {@code wiki_lang} cookie value. */
    @ModelAttribute("m")
    public Map<String, String> messages() {
        return uiMessages.forLanguage(wikiLanguage.getCurrent());
    }

    @ModelAttribute("ui")
    public UiMessageHelper uiHelper() {
        return new UiMessageHelper(uiMessages, wikiLanguage.getCurrent());
    }
}