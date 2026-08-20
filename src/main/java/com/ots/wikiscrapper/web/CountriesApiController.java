package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.data.CountryQueryService;
import com.ots.wikiscrapper.data.CountryRepository;
import com.ots.wikiscrapper.domain.CountryDto;
import com.ots.wikiscrapper.domain.PagedResult;
import com.ots.wikiscrapper.domain.WikiLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for world countries and their Wikipedia summaries.
 */
@RestController
@RequestMapping("/api/countries")
@Tag(name = "Countries")
public class CountriesApiController {

    private final CountryQueryService countryQueryService;
    private final CountryRepository countryRepository;

    public CountriesApiController(CountryQueryService countryQueryService, CountryRepository countryRepository) {
        this.countryQueryService = countryQueryService;
        this.countryRepository = countryRepository;
    }

    @GetMapping
    @Operation(summary = "List countries with optional search, fetch filter, sort, and paging")
    public PagedResult<CountryDto> get(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean fetched,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(required = false) String lang) {
        WikiLanguage language = WikiLanguage.parse(lang);
        return countryQueryService.getPaged(search, page, pageSize, fetched, sort, dir, language);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a country by id")
    public ResponseEntity<CountryDto> getById(
            @PathVariable int id,
            @RequestParam(required = false) String lang) {
        WikiLanguage language = WikiLanguage.parse(lang);
        return countryRepository.findById(id)
                .map(c -> ResponseEntity.ok(CountryDto.from(c, language)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
