package io.github.ladium1.erp.contract.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 계약 진행 상태.
 * <p>
 * 마일스톤 일자 (발주일 / 입고일 / 설치 완료일 / 정산 완료일) 와 연동되는 것이 정상 흐름이지만
 * 강제하지 않는다 — 과거 계약 수기 입력 / 정정 케이스를 막지 않기 위해 사용자가 직접 선택 (FE 가 제안만).
 */
@Getter
@RequiredArgsConstructor
public enum ContractStatus {

    CONTRACTED("계약"),
    ORDERED("발주"),
    ARRIVED("입고"),
    INSTALLING("설치중"),
    INSTALLED("설치완료"),
    SETTLED("정산완료"),
    CANCELED("계약취소");

    private final String description;
}
