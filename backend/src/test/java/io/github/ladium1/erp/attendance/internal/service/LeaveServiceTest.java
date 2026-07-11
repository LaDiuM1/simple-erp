package io.github.ladium1.erp.attendance.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalApi;
import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;
import io.github.ladium1.erp.attendance.internal.dto.LeaveAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceUpdateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveCreateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveSearchCondition;
import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;
import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import io.github.ladium1.erp.attendance.internal.entity.LeaveStatus;
import io.github.ladium1.erp.attendance.internal.entity.LeaveType;
import io.github.ladium1.erp.attendance.internal.exception.AttendanceErrorCode;
import io.github.ladium1.erp.attendance.internal.repository.LeaveRequestRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @InjectMocks
    private LeaveService leaveService;

    @InjectMocks
    private LeaveApprovalResultHandler leaveApprovalResultHandler;

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private LeaveBalanceProvider leaveBalanceProvider;
    @Mock private EmployeeApi employeeApi;
    @Mock private ApprovalApi approvalApi;

    private static final String LOGIN_ID = "testUser";
    private static final Long EMPLOYEE_ID = 1L;
    private static final List<Long> APPROVER_IDS = List.of(2L);

    // 2026-07-06 = 월요일
    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 6);

    private final EmployeeInfo employee = EmployeeInfo.builder()
            .id(EMPLOYEE_ID)
            .loginId(LOGIN_ID)
            .name("테스트직원")
            .build();

    private LeaveBalance balance(String granted, String used) {
        return LeaveBalance.builder()
                .employeeId(EMPLOYEE_ID)
                .year(2026)
                .grantedDays(new BigDecimal(granted))
                .usedDays(new BigDecimal(used))
                .build();
    }

    private LeaveRequest leaveRequest(String days) {
        return LeaveRequest.builder()
                .employeeId(EMPLOYEE_ID)
                .leaveType(LeaveType.ANNUAL)
                .startDate(MONDAY)
                .endDate(MONDAY.plusDays(1))
                .days(new BigDecimal(days))
                .reason("여름 휴가")
                .build();
    }

    private void givenEmployee() {
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
    }

    private void givenSaveEchoes() {
        given(leaveRequestRepository.save(any(LeaveRequest.class))).willAnswer(inv -> inv.getArgument(0));
    }

    private void givenNoPendingDays() {
        given(leaveRequestRepository.sumInProgressDays(eq(EMPLOYEE_ID), anyCollection(), eq(2026)))
                .willReturn(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("연차 일수 계산 — 주말 제외")
    void create_annual_excludes_weekend() {
        // given — 월 ~ 일요일 7일 구간, 주말 제외 5일
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(6), "여름 휴가", APPROVER_IDS);
        givenEmployee();
        given(leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026)).willReturn(balance("15", "0"));
        givenNoPendingDays();
        givenSaveEchoes();
        given(approvalApi.submit(any(ApprovalSubmitCommand.class))).willReturn(100L);

        // when
        leaveService.create(LOGIN_ID, request);

        // then
        ArgumentCaptor<LeaveRequest> leaveCaptor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository).save(leaveCaptor.capture());
        assertThat(leaveCaptor.getValue().getDays()).isEqualByComparingTo("5");
        assertThat(leaveCaptor.getValue().getStatus()).isEqualTo(LeaveStatus.IN_PROGRESS);
        assertThat(leaveCaptor.getValue().getApprovalDocumentId()).isEqualTo(100L);

        ArgumentCaptor<ApprovalSubmitCommand> commandCaptor = ArgumentCaptor.forClass(ApprovalSubmitCommand.class);
        verify(approvalApi).submit(commandCaptor.capture());
        assertThat(commandCaptor.getValue().docType()).isEqualTo(ApprovalDocType.LEAVE);
        assertThat(commandCaptor.getValue().drafterId()).isEqualTo(EMPLOYEE_ID);
        assertThat(commandCaptor.getValue().approverIds()).isEqualTo(APPROVER_IDS);
    }

    @Test
    @DisplayName("금~월 연차는 주말 빼고 2일")
    void create_annual_fri_to_mon_two_days() {
        // given — 금요일 시작, 월요일 종료 (토 / 일 미산입)
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY.plusDays(4), MONDAY.plusDays(7), "주말 낀 연차", APPROVER_IDS);
        givenEmployee();
        given(leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026)).willReturn(balance("15", "0"));
        givenNoPendingDays();
        givenSaveEchoes();
        given(approvalApi.submit(any(ApprovalSubmitCommand.class))).willReturn(100L);

        // when
        leaveService.create(LOGIN_ID, request);

        // then
        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getDays()).isEqualByComparingTo("2");
    }

    @Test
    @DisplayName("반차는 0.5일 고정")
    void create_half_day_success() {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.HALF_DAY_AM, MONDAY, MONDAY, "오전 반차", APPROVER_IDS);
        givenEmployee();
        given(leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026)).willReturn(balance("15", "0"));
        givenNoPendingDays();
        givenSaveEchoes();
        given(approvalApi.submit(any(ApprovalSubmitCommand.class))).willReturn(100L);

        // when
        leaveService.create(LOGIN_ID, request);

        // then
        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getDays()).isEqualByComparingTo("0.5");
    }

    @Test
    @DisplayName("반차 기간이 하루 아니면 400")
    void create_half_day_fail_period_mismatch() {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.HALF_DAY_PM, MONDAY, MONDAY.plusDays(1), "오후 반차", APPROVER_IDS);
        givenEmployee();

        // when & then
        assertThatThrownBy(() -> leaveService.create(LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.INVALID_LEAVE_PERIOD);
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 400")
    void create_fail_start_after_end() {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY.plusDays(3), MONDAY, "역순 기간", APPROVER_IDS);
        givenEmployee();

        // when & then
        assertThatThrownBy(() -> leaveService.create(LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.INVALID_LEAVE_PERIOD);
    }

    @Test
    @DisplayName("주말만 낀 0일 신청 시 400")
    void create_fail_zero_days_weekend_only() {
        // given — 토 ~ 일요일, 주말 제외 일수 0
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY.plusDays(5), MONDAY.plusDays(6), "주말 신청", APPROVER_IDS);
        givenEmployee();

        // when & then
        assertThatThrownBy(() -> leaveService.create(LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.INVALID_LEAVE_PERIOD);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("겹치는 기간 신청 시 409")
    void create_fail_duplicate_period() {
        // given — 같은 기간에 결재 중 / 승인 상태 신청이 이미 존재
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(1), "중복 신청", APPROVER_IDS);
        givenEmployee();
        given(leaveRequestRepository.existsOverlappingPeriod(EMPLOYEE_ID, MONDAY, MONDAY.plusDays(1)))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> leaveService.create(LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.DUPLICATE_LEAVE_PERIOD);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("잔여 부족 시 409")
    void create_fail_insufficient_balance() {
        // given — 잔여 1일, 신청 5일
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(6), "여름 휴가", APPROVER_IDS);
        givenEmployee();
        given(leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026)).willReturn(balance("15", "14"));
        givenNoPendingDays();

        // when & then
        assertThatThrownBy(() -> leaveService.create(LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.INSUFFICIENT_LEAVE_BALANCE);
    }

    @Test
    @DisplayName("결재 중 신청 합산 후 잔여 부족 시 409")
    void create_fail_pending_in_progress_exceeds_balance() {
        // given — 잔여 10일이지만 결재 중 8일 선반영 -> 가용 2일 < 신청 5일
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(4), "여름 휴가", APPROVER_IDS);
        givenEmployee();
        given(leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026)).willReturn(balance("15", "5"));
        given(leaveRequestRepository.sumInProgressDays(eq(EMPLOYEE_ID), anyCollection(), eq(2026)))
                .willReturn(new BigDecimal("8"));

        // when & then
        assertThatThrownBy(() -> leaveService.create(LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.INSUFFICIENT_LEAVE_BALANCE);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("병가는 잔여 검증 없이 신청")
    void create_sick_skips_balance_check() {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.SICK, MONDAY, MONDAY.plusDays(1), "병가", APPROVER_IDS);
        givenEmployee();
        givenSaveEchoes();
        given(approvalApi.submit(any(ApprovalSubmitCommand.class))).willReturn(100L);

        // when
        leaveService.create(LOGIN_ID, request);

        // then
        verify(leaveBalanceProvider, never()).getOrCreate(anyLong(), anyInt());
    }

    @Test
    @DisplayName("기타 휴가는 잔여 검증 없이 신청")
    void create_etc_skips_balance_check() {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ETC, MONDAY, MONDAY.plusDays(1), "경조사", APPROVER_IDS);
        givenEmployee();
        givenSaveEchoes();
        given(approvalApi.submit(any(ApprovalSubmitCommand.class))).willReturn(100L);

        // when
        leaveService.create(LOGIN_ID, request);

        // then
        verify(leaveBalanceProvider, never()).getOrCreate(anyLong(), anyInt());
    }

    @Test
    @DisplayName("승인 콜백 — 잠금 조회 후 APPROVED 전이 + 잔여 차감")
    void on_approved_deducts_balance() {
        // given
        LeaveRequest leave = leaveRequest("2");
        LeaveBalance balance = balance("15", "3");
        given(leaveRequestRepository.findById(10L)).willReturn(Optional.of(leave));
        given(leaveBalanceProvider.getOrCreateWithLock(EMPLOYEE_ID, 2026)).willReturn(balance);

        // when
        leaveApprovalResultHandler.onApproved(10L);

        // then
        assertThat(leave.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(balance.getUsedDays()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("승인 콜백 — 잠금 재검증 잔여 부족 시 실패 (롤백)")
    void on_approved_fail_insufficient_after_lock() {
        // given — 신청 이후 다른 승인으로 잔여가 1일로 줄어든 상황에서 2일 승인
        LeaveRequest leave = leaveRequest("2");
        LeaveBalance balance = balance("15", "14");
        given(leaveRequestRepository.findById(10L)).willReturn(Optional.of(leave));
        given(leaveBalanceProvider.getOrCreateWithLock(EMPLOYEE_ID, 2026)).willReturn(balance);

        // when & then
        assertThatThrownBy(() -> leaveApprovalResultHandler.onApproved(10L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.INSUFFICIENT_LEAVE_BALANCE);
        assertThat(balance.getUsedDays()).isEqualByComparingTo("14");
    }

    @Test
    @DisplayName("반려 콜백 — REJECTED 전이, 차감 없음")
    void on_rejected_no_deduction() {
        // given
        LeaveRequest leave = leaveRequest("2");
        given(leaveRequestRepository.findById(10L)).willReturn(Optional.of(leave));

        // when
        leaveApprovalResultHandler.onRejected(10L);

        // then
        assertThat(leave.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        verify(leaveBalanceProvider, never()).getOrCreateWithLock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("잔여 조회 — 행 없으면 기본 15일")
    void get_my_balance_default_when_absent() {
        // given
        givenEmployee();
        given(leaveBalanceProvider.find(EMPLOYEE_ID, 2026)).willReturn(Optional.empty());

        // when
        LeaveBalanceResponse response = leaveService.getMyBalance(LOGIN_ID, 2026);

        // then
        assertThat(response.grantedDays()).isEqualByComparingTo("15");
        assertThat(response.usedDays()).isEqualByComparingTo("0");
        assertThat(response.remainingDays()).isEqualByComparingTo("15");
        verify(leaveBalanceProvider, never()).getOrCreate(anyLong(), anyInt());
    }

    @Test
    @DisplayName("관리자 목록 — employeeName 벌크 매핑")
    void search_maps_employee_names() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        given(leaveRequestRepository.search(any(LeaveSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(leaveRequest("2")), pageable, 1));
        given(employeeApi.findByIds(List.of(EMPLOYEE_ID))).willReturn(List.of(employee));

        // when
        PageResponse<LeaveAdminResponse> response = leaveService.search(
                new LeaveSearchCondition(null, null, null, null), pageable);

        // then
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content().get(0).employeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(response.content().get(0).employeeName()).isEqualTo("테스트직원");
        assertThat(response.content().get(0).status()).isEqualTo(LeaveStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("관리자 잔여 목록 — 행 없는 직원은 기본 15/0/15 합성")
    void get_balances_synthesizes_missing_rows() {
        // given — 직원 1 은 잔여 행 보유, 직원 2 는 미보유
        EmployeeInfo other = EmployeeInfo.builder().id(2L).loginId("other").name("직원2").build();
        given(employeeApi.findAllActive()).willReturn(List.of(employee, other));
        given(leaveBalanceProvider.findAllByYear(2026)).willReturn(List.of(balance("15", "3")));

        // when
        List<LeaveBalanceAdminResponse> responses = leaveService.getBalances(2026);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).employeeName()).isEqualTo("테스트직원");
        assertThat(responses.get(0).remainingDays()).isEqualByComparingTo("12");
        assertThat(responses.get(1).employeeName()).isEqualTo("직원2");
        assertThat(responses.get(1).grantedDays()).isEqualByComparingTo("15");
        assertThat(responses.get(1).usedDays()).isEqualByComparingTo("0");
        assertThat(responses.get(1).remainingDays()).isEqualByComparingTo("15");
    }

    @Test
    @DisplayName("부여 일수 조정 — getOrCreate 후 변경, 사용 일수 유지")
    void change_granted_days_success() {
        // given
        LeaveBalance balance = balance("15", "3");
        given(leaveBalanceProvider.getOrCreate(EMPLOYEE_ID, 2026)).willReturn(balance);

        // when
        leaveService.changeGrantedDays(EMPLOYEE_ID, new LeaveBalanceUpdateRequest(2026, new BigDecimal("20")));

        // then
        assertThat(balance.getGrantedDays()).isEqualByComparingTo("20");
        assertThat(balance.getUsedDays()).isEqualByComparingTo("3");
        assertThat(balance.remainingDays()).isEqualByComparingTo("17");
    }
}
