package io.github.ladium1.erp.board.internal.dto;

import io.github.ladium1.erp.board.internal.entity.BoardCategory;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        BoardCategory category,
        String title,
        String authorName,
        long commentCount,
        LocalDateTime createdAt
) {
}
