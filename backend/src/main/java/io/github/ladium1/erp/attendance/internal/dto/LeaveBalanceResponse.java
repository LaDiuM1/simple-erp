package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;

import java.math.BigDecimal;

public record LeaveBalanceResponse(
        int year,
        BigDecimal grantedDays,
        BigDecimal usedDays,
        BigDecimal remainingDays
) {

    public static LeaveBalanceResponse from(LeaveBalance balance) {
        return new LeaveBalanceResponse(
                balance.getYear(),
                balance.getGrantedDays(),
                balance.getUsedDays(),
                balance.remainingDays()
        );
    }

    /** 아직 잔여 행이 없는 연도의 기본값 — 조회만으로는 행을 만들지 않는다. */
    public static LeaveBalanceResponse defaultOf(int year, BigDecimal grantedDays) {
        return new LeaveBalanceResponse(year, grantedDays, BigDecimal.ZERO, grantedDays);
    }
}
