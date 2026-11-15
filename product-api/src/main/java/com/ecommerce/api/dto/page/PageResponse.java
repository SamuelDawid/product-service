package com.ecommerce.api.dto.page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public PageResponse {
        content = List.copyOf(content);
    }

    public boolean hasNext() {
        return page + 1 < totalPages;
    }
}
