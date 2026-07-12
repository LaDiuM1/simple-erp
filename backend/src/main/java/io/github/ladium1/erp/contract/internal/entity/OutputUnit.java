package io.github.ladium1.erp.contract.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설비 출력 단위 — 레이저 (kW) / 절곡기 (ton).
 * 출력은 같은 모델도 계약마다 달라지는 사양이라 제품 마스터가 아닌 계약 필드다.
 */
@Getter
@RequiredArgsConstructor
public enum OutputUnit {

    KW("kW"),
    TON("ton");

    private final String description;
}
