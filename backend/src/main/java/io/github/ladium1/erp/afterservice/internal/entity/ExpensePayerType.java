package io.github.ladium1.erp.afterservice.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 경비 결제 주체 — 엑셀에서 "대성결제" 텍스트로만 구분되던 것을 명시 필드로 분리.
 */
@Getter
@RequiredArgsConstructor
public enum ExpensePayerType {

    COMPANY("회사 직접결제"),
    ENGINEER("엔지니어 청구");

    private final String description;
}
