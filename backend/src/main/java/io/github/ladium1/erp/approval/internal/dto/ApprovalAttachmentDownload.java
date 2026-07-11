package io.github.ladium1.erp.approval.internal.dto;

/**
 * 결재 첨부 다운로드 결과 — 표시명 / contentType / 본체 모두 storage 메타에서 온다.
 */
public record ApprovalAttachmentDownload(
        String name,
        String contentType,
        byte[] content
) {
}
