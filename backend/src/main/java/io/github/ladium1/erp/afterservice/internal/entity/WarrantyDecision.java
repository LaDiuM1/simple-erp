package io.github.ladium1.erp.afterservice.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 유상 / 무상 판정 — 설비 대장의 보증 만료일로 자동 제안하되 담당자가 수동 확정한다
 * (발진기 / 그외 이원 보증이라 고장 부위에 따라 판단이 갈리는 실무 반영).
 */
@Getter
@RequiredArgsConstructor
public enum WarrantyDecision {

    UNDECIDED("미확정"),
    FREE("무상"),
    PAID("유상");

    private final String description;
}
