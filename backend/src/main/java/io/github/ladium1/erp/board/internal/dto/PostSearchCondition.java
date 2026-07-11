package io.github.ladium1.erp.board.internal.dto;

import io.github.ladium1.erp.board.internal.entity.BoardCategory;

public record PostSearchCondition(
        BoardCategory category,
        String keyword,
        Long authorId
) {
}
