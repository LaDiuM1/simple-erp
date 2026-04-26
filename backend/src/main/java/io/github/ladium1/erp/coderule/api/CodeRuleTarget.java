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

    DEPARTMENT("부서 코드");

    private final String label;
}
