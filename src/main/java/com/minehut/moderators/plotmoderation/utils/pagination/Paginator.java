package com.minehut.moderators.plotmoderation.utils.pagination;

import java.util.Collections;
import java.util.List;

public class Paginator<T> {

    private final int entriesPerPage;

    public Paginator(int entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
    }

    public PaginatedResult<T> paginate(List<T> entries, int page) {
        if (page < 1 || entries.isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), 0);
        }

        final int totalPages = Math.max((int) Math.ceil((double) entries.size() / entriesPerPage), 1);
        final int startIndex = (page - 1) * entriesPerPage;

        final int fromIndex = Math.min(startIndex, entries.size());
        final int toIndex = Math.min(startIndex + entriesPerPage, entries.size());

        return new PaginatedResult<>(entries.subList(fromIndex, toIndex), totalPages);
    }

    public record PaginatedResult<T>(List<T> items, int totalPages) {

    }

}