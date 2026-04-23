package io.github.ladium1.erp.global.web;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    public <R> PageResponse<R> map(java.util.function.Function<T, R> mapper) {
        return new PageResponse<>(
                content.stream().map(mapper).toList(),
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }
}
