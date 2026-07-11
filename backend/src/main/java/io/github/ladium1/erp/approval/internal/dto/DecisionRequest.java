package io.github.ladium1.erp.approval.internal.dto;

/**
 * 승인 / 반려 결정 요청 — 의견은 선택.
 */
public record DecisionRequest(
        String comment
) {
}
