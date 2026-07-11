package io.github.ladium1.erp.attendance.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 휴가 유형 — deductible 이 true 인 유형만 연차 잔여에서 차감.
 */
@Getter
@RequiredArgsConstructor
public enum LeaveType {

    ANNUAL("연차", true),
    HALF_DAY_AM("오전 반차", true),
    HALF_DAY_PM("오후 반차", true),
    SICK("병가", false),
    ETC("기타", false);

    private final String label;
    private final boolean deductible;

    public boolean isHalfDay() {
        return this == HALF_DAY_AM || this == HALF_DAY_PM;
    }
}
