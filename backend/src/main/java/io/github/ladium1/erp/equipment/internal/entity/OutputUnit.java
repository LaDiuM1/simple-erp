package io.github.ladium1.erp.equipment.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설비 출력 단위 — 레이저 (kW) / 절곡기 (ton).
 * contract 모듈의 동명 enum 과 같은 개념이지만 모듈 내부 enum 은 경계 밖으로 노출하지 않는 규칙이라
 * 각 모듈이 자기 사본을 갖는다 (이벤트로는 name 문자열 전달).
 */
@Getter
@RequiredArgsConstructor
public enum OutputUnit {

    KW("kW"),
    TON("ton");

    private final String description;
}
