package com.ots.wikiscrapper.domain;

import java.util.List;

/** Generic wrapper for paginated query results, used by both the MVC views and the REST API. */
public record PagedResult<T>(List<T> items, long totalCount, int page, int pageSize) {
    public int totalPages() {
        return pageSize <= 0 ? 0 : (int) Math.ceil((double) totalCount / pageSize);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages();
    }
}
