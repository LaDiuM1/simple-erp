package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;

import java.math.BigDecimal;

public record LeaveBalanceAdminResponse(
        Long employeeId,
        String employeeName,
        int year,
        BigDecimal grantedDays,
        BigDecimal usedDays,
        BigDecimal remainingDays
) {

    public static LeaveBalanceAdminResponse from(LeaveBalance balance, String employeeName) {
        return new LeaveBalanceAdminResponse(
                balance.getEmployeeId(),
                employeeName,
                balance.getYear(),
                balance.getGrantedDays(),
                balance.getUsedDays(),
                balance.remainingDays()
        );
    }

    /** 잔여 행이 없는 직원의 합성 기본값 — 조회만으로는 행을 만들지 않는다. */
    public static LeaveBalanceAdminResponse defaultOf(Long employeeId, String employeeName, int year, BigDecimal grantedDays) {
        return new LeaveBalanceAdminResponse(employeeId, employeeName, year, grantedDays, BigDecimal.ZERO, grantedDays);
    }
}
