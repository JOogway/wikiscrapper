package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.data.AppLogRepository;
import com.ots.wikiscrapper.data.CountryRepository;
import com.ots.wikiscrapper.data.VoivodeshipRepository;
import com.ots.wikiscrapper.domain.FetchStats;
import com.ots.wikiscrapper.domain.WikiLanguage;
import com.ots.wikiscrapper.service.SyncJobService;
import com.ots.wikiscrapper.service.WikiLanguageAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** MVC controller for the dashboard — shows sync stats, recent logs, and a trigger button. */
@Controller
public class HomeController {

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VoivodeshipRepository voivodeships;
    private final CountryRepository countries;
    private final AppLogRepository appLogs;
    private final SyncJobService syncJobService;
    private final WikiLanguageAccessor wikiLanguage;
    private final UiMessageCatalog uiMessages;

    public HomeController(
            VoivodeshipRepository voivodeships,
            CountryRepository countries,
            AppLogRepository appLogs,
            SyncJobService syncJobService,
            WikiLanguageAccessor wikiLanguage,
            UiMessageCatalog uiMessages) {
        this.voivodeships = voivodeships;
        this.countries = countries;
        this.appLogs = appLogs;
        this.syncJobService = syncJobService;
        this.wikiLanguage = wikiLanguage;
        this.uiMessages = uiMessages;
    }

    @GetMapping("/")
    public String index(Model model) {
        WikiLanguage language = wikiLanguage.getCurrent();
        FetchStats voivodeshipStats = language == WikiLanguage.Pl
                ? voivodeships.fetchStatsPl()
                : voivodeships.fetchStats();
        FetchStats countryStats = language == WikiLanguage.Pl
                ? countries.fetchStatsPl()
                : countries.fetchStats();
        Instant lastFetched = maxInstant(voivodeshipStats.lastFetchedAt(), countryStats.lastFetchedAt());

        var status = syncJobService.getStatus();
        String progressLabel = "";
        if (status.isRunning()) {
            progressLabel = status.processed() + " / " + status.total();
            if (status.currentItem() != null) {
                progressLabel += " — " + status.currentItem();
            }
        }

        model.addAttribute("voivodeshipCount", voivodeshipStats.total());
        model.addAttribute("voivodeshipsFetched", voivodeshipStats.fetched());
        model.addAttribute("countryCount", countryStats.total());
        model.addAttribute("countriesFetched", countryStats.fetched());
        model.addAttribute("lastFetchedAt", lastFetched == null
                ? uiMessages.get(wikiLanguage.getCurrent(), "common.never")
                : LOCAL.format(lastFetched.atZone(ZoneId.systemDefault())));
        model.addAttribute("recentLogs", appLogs.findTop10ByOrderByCreatedAtDescIdDesc());
        model.addAttribute("status", status);
        model.addAttribute("progressLabel", progressLabel);
        model.addAttribute("isFirstRun", voivodeshipStats.fetched() == 0 && countryStats.fetched() == 0);
        model.addAttribute("percent", status.percent() == null ? 0 : status.percent());
        return "home/index";
    }

    @PostMapping("/sync")
    public String sync(RedirectAttributes redirectAttributes) {
        if (!syncJobService.tryStart()) {
            redirectAttributes.addFlashAttribute(
                    "syncMessage",
                    uiMessages.get(wikiLanguage.getCurrent(), "dashboard.syncAlreadyRunning"));
            redirectAttributes.addFlashAttribute("syncSuccess", false);
        }
        return "redirect:/";
    }

    private static Instant maxInstant(Instant left, Instant right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }
}
