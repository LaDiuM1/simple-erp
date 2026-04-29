package io.github.ladium1.erp.coderule.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채번 규칙 적용 대상.
 * <p>
 * 새 도메인 적용 시 이 enum 에 상수 한 줄 추가 + Initializer 에 기본 규칙 한 건 seed.
 */
@Getter
@RequiredArgsConstructor
public enum CodeRuleTarget {

    DEPARTMENT("부서 코드", true),
    POSITION("직책 코드", false),
    CUSTOMER("고객사 코드", false);

    private final String label;
    /** 도메인이 부모 개념을 갖는지 — 토큰 만들기 UI 에서 부모 토큰 노출 여부 결정 */
    private final boolean hasParent;
}
