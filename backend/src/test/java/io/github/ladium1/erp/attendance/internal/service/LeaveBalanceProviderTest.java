package io.github.ladium1.erp.attendance.internal.service;

import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;
import io.github.ladium1.erp.attendance.internal.repository.LeaveBalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceProviderTest {

    @InjectMocks
    private LeaveBalanceProvider leaveBalanceProvider;

    @Mock private LeaveBalanceRepository leaveBalanceRepository;

    private static final Long EMPLOYEE_ID = 1L;

    private LeaveBalance existingBalance() {
        return LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID)
                .year(2026)
                .grantedDays(new BigDecimal("15"))
                .usedDays(new BigDecimal("3"))
                .build();
    }

    @Test
    @DisplayName("잔여 없으면 기본 15일 자동 생성")
    void get_or_create_creates_default_balance() {
        // given
        given(leaveBalanceRepository.findByEmployeeIdAndYear(EMPLOYEE_ID, 2026)).willReturn(Optional.empty());
        given(leaveBalanceRepository.save(any(LeaveBalance.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        LeaveBalance balance = leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026);

        // then
        ArgumentCaptor<LeaveBalance> captor = ArgumentCaptor.forClass(LeaveBalance.class);
        verify(leaveBalanceRepository).save(captor.capture());
        assertThat(captor.getValue().getGrantedDays()).isEqualByComparingTo("15");
        assertThat(captor.getValue().getUsedDays()).isEqualByComparingTo("0");
        assertThat(balance.remainingDays()).isEqualByComparingTo("15");
    }

    @Test
    @DisplayName("잔여 있으면 그대로 반환, 저장 없음")
    void get_or_create_returns_existing() {
        // given
        LeaveBalance existing = existingBalance();
        given(leaveBalanceRepository.findByEmployeeIdAndYear(EMPLOYEE_ID, 2026)).willReturn(Optional.of(existing));

        // when
        LeaveBalance balance = leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026);

        // then
        assertThat(balance).isSameAs(existing);
        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("잠금 조회 — 행 있으면 그대로 반환")
    void get_or_create_with_lock_returns_existing() {
        // given
        LeaveBalance existing = existingBalance();
        given(leaveBalanceRepository.findWithLockByEmployeeIdAndYear(EMPLOYEE_ID, 2026))
                .willReturn(Optional.of(existing));

        // when
        LeaveBalance balance = leaveBalanceProvider.getOrCreateWithLock(EMPLOYEE_ID, 2026);

        // then
        assertThat(balance).isSameAs(existing);
        verify(leaveBalanceRepository, never()).saveAndFlush(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("잠금 조회 — 행 없으면 기본 15일 생성")
    void get_or_create_with_lock_creates_default() {
        // given
        given(leaveBalanceRepository.findWithLockByEmployeeIdAndYear(EMPLOYEE_ID, 2026))
                .willReturn(Optional.empty());
        given(leaveBalanceRepository.saveAndFlush(any(LeaveBalance.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        LeaveBalance balance = leaveBalanceProvider.getOrCreateWithLock(EMPLOYEE_ID, 2026);

        // then
        assertThat(balance.getGrantedDays()).isEqualByComparingTo("15");
        assertThat(balance.remainingDays()).isEqualByComparingTo("15");
    }

    @Test
    @DisplayName("생성 경합 — 유니크 위반 시 잠금 재조회")
    void get_or_create_with_lock_race_refetches() {
        // given — 첫 잠금 조회는 비어 있고, 동시 생성이 먼저 들어가 saveAndFlush 가 유니크 위반
        LeaveBalance existing = existingBalance();
        given(leaveBalanceRepository.findWithLockByEmployeeIdAndYear(EMPLOYEE_ID, 2026))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(existing));
        given(leaveBalanceRepository.saveAndFlush(any(LeaveBalance.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        // when
        LeaveBalance balance = leaveBalanceProvider.getOrCreateWithLock(EMPLOYEE_ID, 2026);

        // then
        assertThat(balance).isSameAs(existing);
    }
}
