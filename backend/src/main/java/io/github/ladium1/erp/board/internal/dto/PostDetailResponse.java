package io.github.ladium1.erp.board.internal.dto;

import io.github.ladium1.erp.board.internal.entity.BoardCategory;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        BoardCategory category,
        String title,
        String content,
        Long authorId,
        String authorName,
        LocalDateTime createdAt,
        List<PostAttachmentResponse> attachments,
        List<PostCommentResponse> comments
) {
}
