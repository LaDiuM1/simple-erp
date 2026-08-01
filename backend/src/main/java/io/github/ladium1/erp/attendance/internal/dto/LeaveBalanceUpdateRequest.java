package io.github.ladium1.erp.attendance.internal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * 관리자 부여 일수 조정 요청 — 해당 연도 잔여 행이 없으면 생성 후 조정.
 */
public record LeaveBalanceUpdateRequest(
        @NotNull Integer year,
        @NotNull @PositiveOrZero
        @Digits(integer = 4, fraction = 1)
        @DecimalMax("9999.9")
        BigDecimal grantedDays
) {
}
