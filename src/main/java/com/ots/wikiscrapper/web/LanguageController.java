package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.domain.WikiLanguage;
import com.ots.wikiscrapper.service.WikiLanguageAccessor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Sets the Wikipedia display language cookie and redirects back. */
@Controller
public class LanguageController {

    @GetMapping("/language")
    public String setLanguage(
            @RequestParam String lang,
            @RequestParam(defaultValue = "/") String returnUrl,
            HttpServletResponse response) {
        if (!isLocalUrl(returnUrl)) {
            returnUrl = "/";
        }

        Cookie cookie = new Cookie(WikiLanguageAccessor.COOKIE_NAME, WikiLanguage.parse(lang).toCode());
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
        return "redirect:" + returnUrl;
    }

    private static boolean isLocalUrl(String url) {
        return url != null && url.startsWith("/") && !url.startsWith("//");
    }
}
