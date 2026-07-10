package io.github.ladium1.erp.attendance.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 직원 연도별 연차 잔여 — 차감 대상 유형 첫 신청 시 기본 부여 일수로 자동 생성.
 * usedDays 증가는 결재 승인 콜백 시점에만 일어난다.
 */
@Entity
@Getter
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_leave_balances_employee_year",
                columnNames = {"employee_id", "year"}
        ))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            comment = "직원 ID — employee 모듈 참조 (bare Long)")
    private Long employeeId;

    @Column(nullable = false,
            comment = "귀속 연도")
    private int year;

    @Column(nullable = false, precision = 5, scale = 1,
            comment = "부여 일수")
    private BigDecimal grantedDays;

    @Column(nullable = false, precision = 5, scale = 1,
            comment = "사용 일수 — 승인 콜백에서만 증가")
    private BigDecimal usedDays;

    @Builder
    LeaveBalance(Long employeeId, int year, BigDecimal grantedDays, BigDecimal usedDays) {
        this.employeeId = employeeId;
        this.year = year;
        this.grantedDays = grantedDays;
        this.usedDays = usedDays;
    }

    public BigDecimal remainingDays() {
        return grantedDays.subtract(usedDays);
    }

    public void use(BigDecimal days) {
        this.usedDays = this.usedDays.add(days);
    }

    /** 관리자 부여 일수 조정 — 사용 일수는 유지. */
    public void changeGrantedDays(BigDecimal grantedDays) {
        this.grantedDays = grantedDays;
    }
}
