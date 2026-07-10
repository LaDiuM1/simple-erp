package io.github.ladium1.erp.board.internal.dto;

import java.time.LocalDateTime;

public record PostCommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt
) {
}
