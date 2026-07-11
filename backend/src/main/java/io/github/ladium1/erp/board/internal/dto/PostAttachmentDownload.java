package io.github.ladium1.erp.board.internal.dto;

/**
 * 게시글 첨부 다운로드 결과 — 파일명 / contentType / 본체는 storage 메타에서 온다.
 */
public record PostAttachmentDownload(
        String name,
        String contentType,
        byte[] content
) {
}
