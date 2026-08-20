package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.data.VoivodeshipRepository;
import com.ots.wikiscrapper.domain.VoivodeshipDto;
import com.ots.wikiscrapper.domain.WikiLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for Polish voivodeships and their Wikipedia summaries.
 */
@RestController
@RequestMapping("/api/voivodeships")
@Tag(name = "Voivodeships")
public class VoivodeshipsApiController {

    private final VoivodeshipRepository voivodeships;

    public VoivodeshipsApiController(VoivodeshipRepository voivodeships) {
        this.voivodeships = voivodeships;
    }

    @GetMapping
    @Operation(summary = "List all voivodeships")
    public List<VoivodeshipDto> getAll(@RequestParam(required = false) String lang) {
        return WikiLanguage.parse(lang) == WikiLanguage.Pl
                ? voivodeships.findListPl()
                : voivodeships.findListEn();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a voivodeship by id")
    public ResponseEntity<VoivodeshipDto> getById(
            @PathVariable int id,
            @RequestParam(required = false) String lang) {
        WikiLanguage language = WikiLanguage.parse(lang);
        return voivodeships.findById(id)
                .map(v -> ResponseEntity.ok(VoivodeshipDto.from(v, language)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
