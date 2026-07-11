package io.github.ladium1.erp.attendance.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalApi;
import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;
import io.github.ladium1.erp.attendance.internal.dto.LeaveAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceUpdateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveCreateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveSearchCondition;
import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;
import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import io.github.ladium1.erp.attendance.internal.entity.LeaveType;
import io.github.ladium1.erp.attendance.internal.exception.AttendanceErrorCode;
import io.github.ladium1.erp.attendance.internal.repository.LeaveRequestRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveService {

    private static final BigDecimal HALF_DAY = new BigDecimal("0.5");

    /** 잔여 차감 대상 유형 — IN_PROGRESS 선반영 합산 쿼리 조건으로 사용. */
    private static final List<LeaveType> DEDUCTIBLE_TYPES =
            Arrays.stream(LeaveType.values()).filter(LeaveType::isDeductible).toList();

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceProvider leaveBalanceProvider;
    private final EmployeeApi employeeApi;
    private final ApprovalApi approvalApi;

    /**
     * 휴가 신청 = 즉시 결재 상신. 잔여 검증만 신청 시점에 하고,
     * 실제 차감은 승인 콜백 ({@link LeaveApprovalResultHandler#onApproved}) 에서.
     */
    @Auditable(menu = Menu.ATTENDANCE, action = AuditAction.CREATE, targetType = "LeaveRequest", targetIdFromReturn = true)
    @Transactional
    public Long create(String loginId, LeaveCreateRequest request) {
        EmployeeInfo employee = resolveEmployee(loginId);
        validatePeriod(request.leaveType(), request.startDate(), request.endDate());
        BigDecimal days = calculateDays(request.leaveType(), request.startDate(), request.endDate());
        if (days.signum() == 0) {
            // 주말만 낀 기간 — 사용 일수 0일 신청은 거부
            throw new BusinessException(AttendanceErrorCode.INVALID_LEAVE_PERIOD);
        }
        if (leaveRequestRepository.existsOverlappingPeriod(employee.id(), request.startDate(), request.endDate())) {
            throw new BusinessException(AttendanceErrorCode.DUPLICATE_LEAVE_PERIOD);
        }
        if (request.leaveType().isDeductible()) {
            validateBalance(employee.id(), request.startDate().getYear(), days);
        }

        LeaveRequest leaveRequest = leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.id())
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .days(days)
                .reason(request.reason())
                .build());

        Long approvalDocumentId = approvalApi.submit(ApprovalSubmitCommand.builder()
                .docType(ApprovalDocType.LEAVE)
                .title(buildApprovalTitle(request))
                .content(request.reason())
                .drafterId(employee.id())
                .refId(leaveRequest.getId())
                .approverIds(request.approverIds())
                .build());
        leaveRequest.linkApprovalDocument(approvalDocumentId);

        return leaveRequest.getId();
    }

    public List<LeaveResponse> getMyLeaves(String loginId) {
        EmployeeInfo employee = resolveEmployee(loginId);
        return leaveRequestRepository.findByEmployeeIdOrderByIdDesc(employee.id()).stream()
                .map(LeaveResponse::from)
                .toList();
    }

    public LeaveBalanceResponse getMyBalance(String loginId, int year) {
        EmployeeInfo employee = resolveEmployee(loginId);
        return leaveBalanceProvider.find(employee.id(), year)
                .map(LeaveBalanceResponse::from)
                .orElseGet(() -> LeaveBalanceResponse.defaultOf(year, LeaveBalanceProvider.DEFAULT_GRANTED_DAYS));
    }

    /** 관리자 휴가 신청 목록 — employeeName 은 벌크 조회 매핑. */
    public PageResponse<LeaveAdminResponse> search(LeaveSearchCondition condition, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.search(condition, pageable);

        List<Long> employeeIds = page.getContent().stream()
                .map(LeaveRequest::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, String> employeeNames = employeeApi.findByIds(employeeIds).stream()
                .collect(Collectors.toMap(EmployeeInfo::id, EmployeeInfo::name));

        return PageResponse.of(page.map(
                leaveRequest -> LeaveAdminResponse.from(leaveRequest, employeeNames.get(leaveRequest.getEmployeeId()))
        ));
    }

    /** 관리자 잔여 목록 — 재직 직원 전체 기준, 잔여 행이 없는 직원은 기본값 합성. */
    public List<LeaveBalanceAdminResponse> getBalances(int year) {
        Map<Long, LeaveBalance> balances = leaveBalanceProvider.findAllByYear(year).stream()
                .collect(Collectors.toMap(LeaveBalance::getEmployeeId, Function.identity()));

        return employeeApi.findAllActive().stream()
                .map(employee -> {
                    LeaveBalance balance = balances.get(employee.id());
                    return balance != null
                            ? LeaveBalanceAdminResponse.from(balance, employee.name())
                            : LeaveBalanceAdminResponse.defaultOf(
                                    employee.id(), employee.name(), year, LeaveBalanceProvider.DEFAULT_GRANTED_DAYS);
                })
                .toList();
    }

    /** 관리자 부여 일수 조정 — 잔여 행이 없으면 생성 후 조정. */
    @Auditable(menu = Menu.ATTENDANCE, action = AuditAction.UPDATE, targetType = "LeaveBalance", targetIdParam = "employeeId")
    @Transactional
    public void changeGrantedDays(Long employeeId, LeaveBalanceUpdateRequest request) {
        leaveBalanceProvider.getOrCreate(employeeId, request.year())
                .changeGrantedDays(request.grantedDays());
    }

    private void validatePeriod(LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(AttendanceErrorCode.INVALID_LEAVE_PERIOD);
        }
        if (leaveType.isHalfDay() && !startDate.equals(endDate)) {
            throw new BusinessException(AttendanceErrorCode.INVALID_LEAVE_PERIOD);
        }
    }

    /** 잔여 검증 — 아직 결재 중인 본인 차감형 신청 일수를 선반영해 승인 시점 초과를 예방. */
    private void validateBalance(Long employeeId, int year, BigDecimal days) {
        LeaveBalance balance = leaveBalanceProvider.getOrCreate(employeeId, year);
        BigDecimal pendingDays = leaveRequestRepository.sumInProgressDays(employeeId, DEDUCTIBLE_TYPES, year);
        if (balance.remainingDays().subtract(pendingDays).compareTo(days) < 0) {
            throw new BusinessException(AttendanceErrorCode.INSUFFICIENT_LEAVE_BALANCE);
        }
    }

    /** 반차 = 0.5 고정, 그 외 = 주말 (토 / 일) 제외 일수. */
    private BigDecimal calculateDays(LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        if (leaveType.isHalfDay()) {
            return HALF_DAY;
        }
        long weekdays = startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
        return BigDecimal.valueOf(weekdays);
    }

    private String buildApprovalTitle(LeaveCreateRequest request) {
        String period = request.startDate().equals(request.endDate())
                ? request.startDate().toString()
                : request.startDate() + " ~ " + request.endDate();
        return "[휴가] " + request.leaveType().getLabel() + " " + period;
    }

    private EmployeeInfo resolveEmployee(String loginId) {
        return employeeApi.findByLoginId(loginId)
                .orElseThrow(() -> new AccessDeniedException("인증된 직원 정보를 찾을 수 없습니다."));
    }
}
