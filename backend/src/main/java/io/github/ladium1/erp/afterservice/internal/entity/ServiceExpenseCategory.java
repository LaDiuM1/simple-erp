package io.github.ladium1.erp.afterservice.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AS 경비 분류 — 서비스 리포트의 경비 컬럼 (일당 / 숙박비 / 식대 / 부품비 / 기타) 미러.
 * 엔지니어 숙소 결제 내역은 별도 시트가 아니라 "숙박비 + 회사 직접결제" 경비 행으로 흡수한다.
 */
@Getter
@RequiredArgsConstructor
public enum ServiceExpenseCategory {

    DAILY_WAGE("일당"),
    LODGING("숙박비"),
    MEAL("식대"),
    PARTS("부품비"),
    ETC("기타");

    private final String description;
}
