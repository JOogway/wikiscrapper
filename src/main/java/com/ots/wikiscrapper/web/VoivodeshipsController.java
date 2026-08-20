package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.data.VoivodeshipRepository;
import com.ots.wikiscrapper.domain.WikiLanguage;
import com.ots.wikiscrapper.service.WikiLanguageAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** MVC controller serving the voivodeships listing page. */
@Controller
public class VoivodeshipsController {

    private final VoivodeshipRepository voivodeships;
    private final WikiLanguageAccessor wikiLanguage;

    public VoivodeshipsController(VoivodeshipRepository voivodeships, WikiLanguageAccessor wikiLanguage) {
        this.voivodeships = voivodeships;
        this.wikiLanguage = wikiLanguage;
    }

    @GetMapping("/voivodeships")
    public String index(Model model) {
        model.addAttribute("voivodeships", wikiLanguage.getCurrent() == WikiLanguage.Pl
                ? voivodeships.findListPl()
                : voivodeships.findListEn());
        return "voivodeships/index";
    }
}
