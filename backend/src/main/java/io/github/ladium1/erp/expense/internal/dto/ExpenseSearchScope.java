package io.github.ladium1.erp.expense.internal.dto;

/**
 * 경비 청구 목록 조회 범위 — MINE 은 본인 청구, ALL 은 정산 관리자 (EXPENSES write) 전용 전체 조회.
 */
public enum ExpenseSearchScope {

    MINE,
    ALL
}
