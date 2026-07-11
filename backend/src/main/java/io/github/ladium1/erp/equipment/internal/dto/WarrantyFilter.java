package io.github.ladium1.erp.equipment.internal.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 목록 검색의 보증 상태 필터.
 * <p>
 * 기준일 = 조회 시점. 만료일이 입력되지 않은 설비 (보증 정보 미보완) 는 어떤 값에도 매칭되지 않는다.
 */
@Getter
@RequiredArgsConstructor
public enum WarrantyFilter {

    /** 무상 AS 유효 (무상 AS 만료일 >= 오늘) */
    ACTIVE("보증중"),
    /** 발진기 / 무상 AS 중 하나라도 90일 내 만료 — 만료 전 선제 대응용 */
    EXPIRING("만료 임박"),
    /** 무상 AS 만료 (무상 AS 만료일 < 오늘) — 유상 전환 대상 */
    EXPIRED("만료");

    private final String description;
}
