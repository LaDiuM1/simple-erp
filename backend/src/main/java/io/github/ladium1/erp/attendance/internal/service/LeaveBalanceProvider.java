package io.github.ladium1.erp.attendance.internal.service;

import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;
import io.github.ladium1.erp.attendance.internal.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 연도별 연차 잔여의 조회 / 기본 생성 단일 지점.
 * 신청 시 잔여 검증 (LeaveService) 과 승인 시 차감 (LeaveApprovalResultHandler) 이 공유한다
 * — 핸들러가 LeaveService 를 직접 의존하면 ApprovalService(핸들러 목록 주입) 와 순환 참조가 생겨 분리.
 */
@Component
@RequiredArgsConstructor
public class LeaveBalanceProvider {

    /** 잔여 행이 없을 때 자동 부여되는 기본 연차 일수. */
    public static final BigDecimal DEFAULT_GRANTED_DAYS = BigDecimal.valueOf(15);

    private final LeaveBalanceRepository leaveBalanceRepository;

    public Optional<LeaveBalance> find(Long employeeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
    }

    /** 연도 전체 잔여 행 — 관리자 잔여 목록 용. */
    public List<LeaveBalance> findAllByYear(int year) {
        return leaveBalanceRepository.findAllByYear(year);
    }

    /** 해당 연도 잔여 행이 없으면 기본 부여 일수로 생성해 반환. */
    public LeaveBalance getOrCreate(Long employeeId, int year) {
        return find(employeeId, year)
                .orElseGet(() -> leaveBalanceRepository.save(defaultBalance(employeeId, year)));
    }

    /**
     * 잠금 (PESSIMISTIC_WRITE) 버전 — 승인 콜백의 잔여 재검증 + 차감이 동시 승인과 직렬화되도록.
     * 생성 경합 시 유니크 제약 위반을 잡아 먼저 들어간 행을 잠금 재조회한다.
     */
    public LeaveBalance getOrCreateWithLock(Long employeeId, int year) {
        return leaveBalanceRepository.findWithLockByEmployeeIdAndYear(employeeId, year)
                .orElseGet(() -> createDefaultWithLock(employeeId, year));
    }

    private LeaveBalance createDefaultWithLock(Long employeeId, int year) {
        try {
            return leaveBalanceRepository.saveAndFlush(defaultBalance(employeeId, year));
        } catch (DataIntegrityViolationException duplicate) {
            return leaveBalanceRepository.findWithLockByEmployeeIdAndYear(employeeId, year)
                    .orElseThrow(() -> duplicate);
        }
    }

    private LeaveBalance defaultBalance(Long employeeId, int year) {
        return LeaveBalance.builder()
                .employeeId(employeeId)
                .year(year)
                .grantedDays(DEFAULT_GRANTED_DAYS)
                .usedDays(BigDecimal.ZERO)
                .build();
    }
}
