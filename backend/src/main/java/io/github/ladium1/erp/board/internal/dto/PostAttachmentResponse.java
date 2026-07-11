package io.github.ladium1.erp.board.internal.dto;

public record PostAttachmentResponse(
        Long fileId,
        String name,
        long size
) {
}
