package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.data.CountryQueryService;
import com.ots.wikiscrapper.domain.CountryDto;
import com.ots.wikiscrapper.domain.CountryListSort;
import com.ots.wikiscrapper.domain.PagedResult;
import com.ots.wikiscrapper.service.WikiLanguageAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** MVC controller for the paginated, searchable, sortable countries listing page. */
@Controller
public class CountriesController {

    /** Chunk size used by the client when page size is {@code all}. */
    public static final int VIRTUAL_CHUNK_SIZE = 50;

    private final CountryQueryService countryQueryService;
    private final WikiLanguageAccessor wikiLanguage;

    public CountriesController(CountryQueryService countryQueryService, WikiLanguageAccessor wikiLanguage) {
        this.countryQueryService = countryQueryService;
        this.wikiLanguage = wikiLanguage;
    }

    @GetMapping("/countries")
    public String index(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fetched,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") String pageSize,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            Model model) {
        boolean virtualizeAll = "all".equalsIgnoreCase(pageSize);
        int resolvedPageSize = virtualizeAll
                ? VIRTUAL_CHUNK_SIZE
                : switch (pageSize) {
                    case "10", "20", "50" -> Integer.parseInt(pageSize);
                    default -> 20;
                };
        Boolean fetchedFilter = switch (fetched == null ? "" : fetched) {
            case "yes" -> true;
            case "no" -> false;
            default -> null;
        };
        String sortColumn = CountryListSort.normalizeColumn(sort);
        String sortDir = CountryListSort.isDescending(dir) ? "desc" : "asc";
        PagedResult<CountryDto> result;
        if (virtualizeAll) {
            long totalCount = countryQueryService.count(search, fetchedFilter, wikiLanguage.getCurrent());
            result = new PagedResult<>(List.of(), totalCount, 1, VIRTUAL_CHUNK_SIZE);
        } else {
            result = countryQueryService.getPaged(
                    search, page, resolvedPageSize, fetchedFilter, sortColumn, sortDir, wikiLanguage.getCurrent());
        }

        String fetchedValue = fetchedFilter == null ? null : fetched;
        model.addAttribute("paged", result);
        model.addAttribute("search", search);
        model.addAttribute("fetched", fetchedValue);
        model.addAttribute("sort", sortColumn);
        model.addAttribute("dir", sortDir);
        model.addAttribute("virtualizeAll", virtualizeAll);
        model.addAttribute("chunkSize", VIRTUAL_CHUNK_SIZE);
        model.addAttribute("startPage", Math.max(1, result.page() - 2));
        model.addAttribute("endPage", Math.min(result.totalPages(), result.page() + 2));
        model.addAttribute("view", new CountriesView(
                search, fetchedValue, sortColumn, sortDir, virtualizeAll ? "all" : String.valueOf(resolvedPageSize)));
        model.addAttribute("pageSizes", List.of(10, 20, 50));

        // Live search sends XMLHttpRequest and only needs the results fragment,
        // not the full page shell (layout, nav, scripts).
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "countries/index :: results";
        }
        return "countries/index";
    }
}
