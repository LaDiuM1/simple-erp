package io.github.ladium1.erp.expense.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 경비 청구 상태 — 결재 결과 콜백으로만 전이.
 */
@Getter
@RequiredArgsConstructor
public enum ExpenseStatus {

    IN_PROGRESS("결재 중"),
    APPROVED("승인"),
    REJECTED("반려");

    private final String label;
}
