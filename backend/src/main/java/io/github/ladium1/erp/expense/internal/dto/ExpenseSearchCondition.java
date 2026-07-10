package io.github.ladium1.erp.expense.internal.dto;

import io.github.ladium1.erp.expense.internal.entity.ExpenseStatus;

import java.time.LocalDate;

/**
 * 경비 청구 검색 조건 — claimantId (본인) 조건은 리포지토리 검색 시 별도 필수 인자로 강제.
 *
 * @param startDate / endDate 청구 생성일 (createdAt) 기간
 */
public record ExpenseSearchCondition(
        ExpenseStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String keyword
) {
}
