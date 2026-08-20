package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.data.AppLogRepository;
import com.ots.wikiscrapper.domain.LogLevel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** MVC controller for the audit-log viewer with optional level filtering. */
@Controller
public class LogsController {

    private final AppLogRepository appLogs;

    public LogsController(AppLogRepository appLogs) {
        this.appLogs = appLogs;
    }

    @GetMapping("/logs")
    public String index(@RequestParam(required = false) String level, Model model) {
        LogLevel levelFilter = parseLevel(level);
        var logs = levelFilter == null
                ? appLogs.findTop100ByOrderByCreatedAtDescIdDesc()
                : appLogs.findTop100ByLevelOrderByCreatedAtDescIdDesc(levelFilter);
        model.addAttribute("logs", logs);
        model.addAttribute("level", levelFilter == null ? null : levelFilter.name());
        return "logs/index";
    }

    private static LogLevel parseLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        try {
            return LogLevel.valueOf(level);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
