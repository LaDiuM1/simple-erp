package io.github.ladium1.erp.expense.internal.dto;

/**
 * 영수증 다운로드 결과 — 파일명 / contentType / 본체는 storage 메타에서 온다.
 */
public record ExpenseReceiptDownload(
        String name,
        String contentType,
        byte[] content
) {
}
